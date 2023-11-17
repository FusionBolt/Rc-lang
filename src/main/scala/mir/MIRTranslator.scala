package rclang
package mir
import ast.*
import mir.IRBuilder

import ast.ImplicitConversions.*
import ty.*

import tools.*

class MethodMapper(val data: List[(Class, Method, Function)]) {
  def find(manglingName: String): Function = {
    data.find((_, _, fn) => manglingName == fn.name) match
      case Some(value) => value._3
      case None => ???
  }

  def toMap: Map[String, Function] = data.map((_, _, fn) => fn.name -> fn).toMap

  override def toString: String = data.mkString
}

private def mangling(fullName: FullName): String = {
  tools.mangling(fullName.fn, List(fullName.module, fullName.klass))
}

class MIRTranslator(globalTable: GlobalTable) {
  def proc(rcModule: RcModule): Module = {
    val mapper = collectFunction(rcModule)
    mapper.data.foreach((klass, method, fn) => {
      MethodTranslator(globalTable, mapper).translate(method, rcModule.name, klass, fn)
    })
    Module(rcModule.name, mapper.toMap)
  }

  def methodPreprocess(klass: Class, method: Method, moduleName: String): Method = {
    // 1. rename
    // 2. add this ptr to args
    val inputs = Params(List(Param(Def.self, TyInfo.Spec(klass.name))) ::: method.decl.inputs.params)
    val decl = method.decl.copy(inputs = inputs)
    method.copy(decl = decl)
  }

  def collectFunction(module: RcModule): MethodMapper = {
    // collect and mangling name, be used to lookup and ref for Call
    val moduleName = module.name
    val methods = module.items.flatMap(_ match
      case m: Method => {
        val fnName = mangling(FullName(m.decl, Def.Kernel, moduleName))
        List((globalTable.kernel.astNode, m, Function.Empty(fnName)))
      }
      case klass: Class => {
        klass.methods.map(method => {
          val fnName = mangling(FullName(method.decl, klass.name.str, moduleName))
          (klass, methodPreprocess(klass, method, module.name), Function.Empty(fnName))
        })
      }
      case _ => throw new Exception("not support"))
    MethodMapper(methods)
  }
}

private class MethodTranslator(globalTable: GlobalTable, methodMapper: MethodMapper) {
  var builder = IRBuilder()

  var env = Map[Ident, Value]()

  var args = List[Argument]()

  var strTable = List[Str]()

  var nestSpace: NestSpace = null

  // used for continue
  var curHeader: BasicBlock = null
  // used for break
  var nextBasicBlock: BasicBlock = null


  def translate(method: Method, moduleName: String, klass: Class, fn: Function): Unit = {
    // todo: klass pass class for nestspace
    nestSpace = NestSpace(globalTable, FullName(method.decl, klass.name, moduleName))
    args = translateParams(method.decl.inputs)
    env = args.map(arg => Ident(arg.name) -> arg).toMap
    builder = IRBuilder()
    // modify fn
    fn.argument = args
    fn.retType = makeType(method.decl.outType)
    fn.setPos(method.pos)
    // init
    builder.currentBasicBlock.parent = fn
    builder.currentFn = fn
    fn.entry = builder.currentBasicBlock
    procBlock(method.body)
    // auto create return
    insertReturn()
    fn.bbs = builder.basicBlocks.filter(_.stmts.nonEmpty)
    fn.entry = builder.basicBlocks.head
    fn.strTable = strTable
  }

  def procBlock(block: Expr.Block): Value = {
    if (block.stmts.isEmpty) {
      NilValue
    } else {
      block.stmts.map(procStmt).last
    }
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
        // todo: fix phi
        phi.addIncoming(t, trueBB)
        f match
          case Some(value) => phi.addIncoming(f.get, falseBB)
          case None =>
        phi
      }
      //      case Expr.Lambda(args, block) => ???
      case Expr.Call(target, args, _) => {
        if (intrinsics.contains(target.str)) {
          builder.createIntrinsic(target.str, args.map(procExpr))
        } else {
          builder.createCall(lookupFn(target), args.map(procExpr))
        }
      }
      case Expr.MethodCall(obj, target, args) => {
        val makeCall = (klass: Ident, fname: Ident, thisPtr: Value) => {
          val f = lookupFn(fname, nestSpace.withClass(klass.str))
          builder.createCall(f, thisPtr +: args.map(procExpr))
        }
        obj match
          // class method
          case Expr.Symbol(klassSym, _) => {
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
          val fieldTy = lookupFieldTy(name, field.str)
          builder.createGetElementPtr(obj, Integer(structType.fieldOffset(field)), fieldTy)
        }
      }
      case Expr.Array(len, initValues) => {
        ConstantArray(len, initValues.map(procExpr))
      }
      case Expr.Index(arr, i) => {
        val index = builder.createLoad(procExpr(i))
        val array = procExpr(arr)
        // index is value
        // src ty, target ty
        builder.createGetElementPtr(array, index, array.ty.asInstanceOf[ArrayType].valueT)
      }
      case Expr.Self => {
        // todo: fix this, in this
        // todo: 有的参数没改变
        builder.createLoad(args(0))
      }
      case _ => Debugger.unImpl(expr)
    if v.ty == InferType then v.withTy(expr.ty) else v
    v.setPos(expr.pos)
  }

  def procStmt(stmt: ast.Stmt): Value = {
    val value = stmt match
      case ast.Stmt.Local(name, tyInfo, value) => {
        val alloc = builder.createAlloc(name.str, stmt.ty)
        builder.createStore(procExpr(value), alloc).setPos(stmt.pos)
        env += (name -> alloc)
        alloc
      }

      case ast.Stmt.Expr(expr) => {
        return procExpr(expr)
      }
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
      case ast.Stmt.For(init, cond, incr, body) => {
        // xxx
        // init -> cond
        // header:
        // incr
        // condBB:
        // cmp body, afterBody
        // body:
        //    continue -> header
        //    continue -> header
        // afterBody:
        // xxx

        val header = builder.createBB()
        val condBB = builder.createBB()
        val bodyBB = builder.createBB()
        val afterBB = builder.createBB()

        curHeader = header
        nextBasicBlock = afterBB
        // prevBB
        procStmt(init)
        builder.createBr(condBB)
        // header: begin with incr
        builder.insertBasicBlock(header)
        procStmt(incr)
        // condBB
        builder.insertBasicBlock(condBB)
        val condValue = procExpr(cond)
        builder.createCondBr(condValue, bodyBB, afterBB)
        // body
        builder.insertBasicBlock(bodyBB)
        val bodyValue = procBlock(body)
        builder.createBr(header)
        // after
        builder.insertBasicBlock(afterBB)
        bodyValue
      }
      case ast.Stmt.Assign(name, value) => {
        builder.createStore(procExpr(value), lookup(name))
      }
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
    value.setPos(stmt.pos)
  }


  private def makeType(tyInfo: TyInfo): Type = {
    tyInfo match
      case TyInfo.Spec(ty) => Infer.translate(ty)
      case TyInfo.Nil => NilType
      case _ => InferType
  }

  private def insertReturn(): Unit = {
    if (builder.currentBasicBlock.stmts.isEmpty) {
      if (builder.basicBlocks.size < 2) { // only current
        builder.createReturn(NilValue)
      } else { // prev bb last value
        builder.createReturn(builder.basicBlocks(builder.basicBlocks.size - 2).stmts.last)
      }
    }
    else if (!builder.currentBasicBlock.stmts.last.isInstanceOf[Return]) {
      builder.createReturn(builder.currentBasicBlock.stmts.last)
    }
  }

  private def translateParams(params: Params): List[Argument] = {
    params.params.map(param => {
      val ty = param.ty
      val name = param.name
      Argument(name.str, makeType(ty))
    })
  }

  def lookupFn(name: Ident, nestSpace: NestSpace = this.nestSpace): Function = {
    // 1. current class
    // 2. parent class
    val firstLookup = globalTable.classTable(nestSpace.klass.name).lookupMethods(name.str, globalTable)
    val (klass, method) = firstLookup match
      case Some(value) => value
      case None => {
        // 3. Kernel class
        globalTable.kernel.lookupMethods(name, globalTable) match
          case Some(value) => value
          // not found, throw error
          case None => ???
      }
    val manglingName = mangling(FullName(method.decl, klass.name.str, nestSpace.fullName.module))
    // find by mangling name
    methodMapper.find(manglingName)
  }

  def lookup(id: Ident): Value = {
    // local var
    // 1. local
    // 2. outer and outer
    env.getOrElse(id, args.find(_.name == id.str).getOrElse({
      procExpr(nestSpace.lookupVar(id))
    }))
    // 3. field
    // 4. Kernel
  }

  def lookupFieldTy(klass: String, field: String) = {
    // 1. klass
    // 2. klass.parent
    globalTable.classTable(klass).lookupFieldTy(field).get
  }
}