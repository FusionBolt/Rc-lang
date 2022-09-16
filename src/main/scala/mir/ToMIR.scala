package rclang
package mir
import ast.*
import mir.IRBuilder

import rclang.ast.ImplicitConversions.*
import ty.*

import tools.*

case class ToMIR(var globalTable: GlobalTable) {
  var module = Module()
  def proc(rcModule: RcModule): Module = {
    module.name = rcModule.name
    rcModule.items.foreach(_ match
      case m: Method => getFn(m)
      case c: Class => procClass(c)
      case _ => throw new Exception("not support"))
    module
  }

  var klass = Def.Kernel
  def procClass(klass: Class) = {
    this.klass = klass.name.str;
    var fns = klass.methods.map(methodPreprocess(klass, _)).map(getFn(_, klass.name.str))
    this.klass = Def.Kernel
  }

  def methodPreprocess(klass: Class, method: Method): Method = {
    // 1. rename
    // 2. add this ptr to args
    val decl = method.decl.copy(inputs = Params(List(Param(Def.self, TyInfo.Spec(klass.name))):::method.decl.inputs.params))
    method.copy(decl = decl)
  }

  def getFn(fn: Method, prefix: String = ""): Function = FnToMIR(globalTable, module, klass).procMethod(fn, prefix)
}

def mangling(fullName: FullName): String = {
    fullName.names.mkString("_")
}

def demangling(name: String): FullName = {
  FullNameMaker.make(name.split("_"))
}

case class FnToMIR(var globalTable: GlobalTable, var parentModule: Module, var klass: String = "") {
  var nestSpace: NestSpace = NestSpace(globalTable, FullName("", klass, parentModule.name))

  def getFunOuterClass(id: Ident, nestSpace: NestSpace, gt: GlobalTable): Class = {
    nestSpace.findMethodInWhichClass(id, gt)
  }

  def getFun(id: Ident, nestSpace: NestSpace = this.nestSpace): Function = {
    // when call a member method, nestSpace.fullName.fn != id
    val klass = getFunOuterClass(id, nestSpace, globalTable).name
    parentModule.fnTable.getOrElse(mangling(nestSpace.fullName.copy(klass = klass, fn = id)), {
      val fn = nestSpace.lookupFn(id)
      val newFn = FnToMIR(globalTable, parentModule, klass).procMethod(fn)
      if(parentModule.fnTable.contains(id.str)) {
        throw new RuntimeException()
      }
      parentModule.fnTable += (id.str -> newFn)
      newFn
    })
  }

  def lookup(id: Ident): Value = {
    // local var
    env.getOrElse(id, args.find(_.name == id.str).getOrElse({
      procExpr(nestSpace.lookupVar(id))
//      throw new RuntimeException()
    }))
  }

  var builder = IRBuilder()

  var env = Map[Ident, Value]()

  var args = List[Argument]()

  var strTable = List[Str]()

  var curHeader: BasicBlock = null
  var nextBasicBlock: BasicBlock = null

  def procArgument(params: Params): List[Argument] = {
    params.params.map(param => {
      val ty = param.ty
      val name = param.name
      Argument(name.str, makeType(ty))
    })
  }

  def procMethod(method: Method, prefix: String = ""): Function = {
    nestSpace = NestSpace(globalTable, FullName(method.name.str, klass, parentModule.name))
    // ir builder manages the function
    args = procArgument(method.decl.inputs)
    env = args.map(arg => Ident(arg.name) -> arg).toMap
//    val fnName = s"${prefix}_${method.decl.name.str}"
    val fnName = mangling(nestSpace.fullName)
    builder = IRBuilder()
    val fn = Function(fnName, makeType(method.decl.outType), args, builder.currentBasicBlock)
    builder.currentBasicBlock.parent = fn
    parentModule.fnTable += (fnName -> fn)
    builder.currentFn = fn
    fn.entry = builder.currentBasicBlock
    procBlock(method.body)
    if(builder.currentBasicBlock.stmts.isEmpty) {
      if(builder.basicBlocks.size < 2) { // only current
        builder.createReturn(NilValue)
      } else { // prev bb last value
        builder.createReturn(builder.basicBlocks(builder.basicBlocks.size - 2).stmts.last)
      }
    }
    else if(!builder.currentBasicBlock.stmts.last.isInstanceOf[Return]) {
      builder.createReturn(builder.currentBasicBlock.stmts.last)
    }
    fn.bbs = builder.basicBlocks.filter(_.stmts.nonEmpty)
    fn.entry = builder.basicBlocks.head
    fn.strTable = strTable
    args = List()
    fn
  }

  def procBlock(block: Expr.Block): Value = {
    if(block.stmts.isEmpty) {
      NilValue
    } else {
      block.stmts.map(procStmt).last
    }
  }

  def currClass: Class = {
    globalTable.classTable(klass).astNode
  }

  def procExpr(expr: Expr): Value = {
    val v = expr match
      case Expr.Number(v) => Integer(v)
      case Expr.Identifier(ident) => builder.createLoad(lookup(ident))
      case Expr.Bool(b) => Bool(b)
      case Expr.Binary(op, lhs, rhs) => {
        val l = procExpr(lhs)
        val r = procExpr(rhs)
        builder.createBinary(op.toString, l, r)
      }
      case Expr.Str(str) => {
        val strV = Str(str)
        strTable = strTable :+ strV
        strV
      }
      case Expr.If(cond, true_branch, false_branch) => {
        val c = procExpr(cond)
        val trueBB = builder.createBB()
        val falseBB = builder.createBB()
        val mergeBB = builder.createBB()
        builder.createCondBr(c, trueBB, falseBB)
        builder.insertBasicBlock(trueBB)
        val t = procExpr(true_branch)
        builder.createBr(mergeBB)
        builder.insertBasicBlock(falseBB)
        val f = false_branch.map(procExpr)
        builder.createBr(mergeBB)
        builder.insertBasicBlock(mergeBB)
        val phi = builder.createPHINode()
        phi.addIncoming(t, trueBB)
        f match
          case Some(value) => phi.addIncoming(f.get, falseBB)
          case None =>
        phi
      }
      //      case Expr.Lambda(args, block) => ???
      case Expr.Call(target, args) => {
        if(intrinsics.contains(target.str)) {
          builder.createIntrinsic(target.str, args.map(procExpr))
        } else {
          builder.createCall(getFun(target), args.map(procExpr))
        }
      }
      case Expr.MethodCall(obj, target, args) => {
        val makeCall = (klass: Ident, fname: Ident, thisPtr: Value) => {
          val f = getFun(fname, nestSpace.withClass(klass.str))
          builder.createCall(f, thisPtr +: args.map(procExpr))
        }
        obj match
          // class method
          case Expr.Symbol(klassSym) => {
//            val klass = globalTable.classTable(klassSym)
            makeCall(klassSym, target, NilValue)
          }
          // instance method
          case _ => {
            val objPtr = builder.createLoad(procExpr(obj))
            structTyProc(objPtr.ty) { case StructType(name, fields) =>
              makeCall(name, target, objPtr)
            }
          }
      }
      case block: Expr.Block => procBlock(block)
      case Expr.Return(expr) => builder.createReturn(procExpr(expr))
      case Expr.Field(expr, field) => {
        val obj = procExpr(expr)
        structTyProc(obj.ty) { case StructType(name, _) =>
          val structType = TypeBuilder.fromClass(name, globalTable)
          val fieldTy = nestSpace.withClass(name).lookupVar(field).ty
          GetElementPtr(obj, structType.fieldOffset(field), fieldTy)
        }
      }
      case _ => ???
      if v.ty == InferType then v.withTy(expr.ty) else v
  }

  def makeType(tyInfo: TyInfo): Type = {
    tyInfo match
      case TyInfo.Spec(ty) => Infer.translate(ty)
      case TyInfo.Nil => NilType
      case _ => InferType
  }

  def procStmt(stmt: ast.Stmt): Value = {
    stmt match
      case ast.Stmt.Local(name, tyInfo, value) => {
        val alloc = builder.createAlloc(name.str, stmt.ty)
        builder.createStore(procExpr(value), alloc)
        env += (name -> alloc)
        alloc
      }

      case ast.Stmt.Expr(expr) => procExpr(expr)
      case ast.Stmt.While(cond, body) => {
        // header
        val header = builder.createBB()
        curHeader = header
        builder.createBr(header)
        builder.insertBasicBlock(header)
        val condValue = procExpr(cond)
        val bodyBB = builder.createBB()
        val afterBB = builder.createBB()
        nextBasicBlock = afterBB
        builder.createCondBr(condValue, bodyBB, afterBB)
        // body
        builder.insertBasicBlock(bodyBB)
        val bodyValue = procBlock(body)
        // after
        builder.createBr(header)
        builder.insertBasicBlock(afterBB)
        bodyValue
      }
      case ast.Stmt.Assign(name, value) => builder.createStore(procExpr(value), lookup(name))
      case ast.Stmt.Break() => {
        val br = builder.createBr(nextBasicBlock)
        builder.insertBasicBlock()
        br
      }
      case ast.Stmt.Continue() => {
        val br = builder.createBr(curHeader)
        builder.insertBasicBlock()
        br
      }
  }
}