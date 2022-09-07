package rclang
package mir
import ast.*
import mir.IRBuilder

import rclang.ast.ImplicitConversions.*
import ty.*

import tools.{ClassEntry, GlobalTable}

// todo:不同层的id怎么处理
// todo:debug info
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
    // todo:rename fn, how to distinct between normal name and link name
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

// FnEnv
// 0. f
// 1. args
// 2. local var
type FnEnv = Map[Ident, Value]

// todo: not support nest class and module

case class FullName(fn: String, klass: String, module: String)

case class NestSpace(val gt: GlobalTable, val fullName: FullName, val fnEnv: FnEnv, val args: List[Argument]) {
  def fn = {
    gt.classTable(fullName.klass).methods(fullName.fn).astNode
  }

  def klass = {
    gt.classTable(fullName.klass).astNode
  }

  def module: RcModule = {
    RcModule(List())
  }

  def lookupFn(id: Ident): Method = {
    // 1. fn
    if(fn.name == id) {
      fn
    } else {
      // 2. class
      klass.methods.find(_.name == id).getOrElse(
      // 3. module
        module.items.find(_ match
        case m: Method => m.name == id
        case _ => false).get.asInstanceOf[Method])
    }
  }

  def lookupVar(id: Ident): Value = {
    // 1. fn
    fnEnv.getOrElse(id, {
      // 2. class
      val index = klass.fieldIndex(id.str)
      // todo: fix index and index
      GetElementPtr(args(0), index)
    })
  }
}


case class FnToMIR(var globalTable: GlobalTable, var parentModule: Module, var klass: String = "") {
  def getFun(id: Ident, prefix: String = ""): Function = {
    val fn = NestSpace(globalTable, FullName(id, klass, parentModule.name), env, args).lookupFn(id)
    val newFn = FnToMIR(globalTable, parentModule).procMethod(fn, prefix)
    parentModule.fnTable += (id.str -> newFn)
    newFn
  }

  def lookup(id: Ident): Value = {
    // local var
    NestSpace(globalTable, FullName(id, klass, parentModule.name), env, args).lookupVar(id)
  }

  var builder = IRBuilder()
  // todo: ident order maybe error??
  var env: FnEnv = Map()
  var strTable = List[Str]()

  var curHeader: BasicBlock = null
  var nextBasicBlock: BasicBlock = null

  var args = List[Argument]()
  // todo: argument and params
  def procArgument(params: Params): List[Argument] = {
    params.params.map(param => {
      val ty = param.ty
      val name = param.name
      Argument(name.str, makeType(ty))
    })
  }

  // todo:implicit TypeInfo to Type
  def procMethod(method: Method, prefix: String = ""): Function = {
    // ir builder manages the function
    args = procArgument(method.decl.inputs)
    env = args.map(arg => Ident(arg.name) -> arg).toMap
//    val fnName = s"${prefix}_${method.decl.name.str}"
    val fnName = method.decl.name.str
    builder = IRBuilder()
    val fn = Function(fnName, makeType(method.decl.outType), args, builder.currentBasicBlock)
    builder.currentBasicBlock.parent = fn
    parentModule.fnTable += (fnName -> fn)
    builder.currentFn = fn
    fn.entry = builder.currentBasicBlock
    procBlock(method.body)
    if(builder.currentBasicBlock.stmts.isEmpty) {
        builder.createReturn(builder.basicBlocks(builder.basicBlocks.size - 2).stmts.last)
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

  // todo:this need block? or add a scope?
  def procBlock(block: Expr.Block): Value = {
    block.stmts.map(procStmt).last
  }

  // todo:finish
//  def getClassMember(id: Ident): Value = {
//    // get alloca of member var
//    globalTable.getOrElse(id, throw new Exception(s"undefined variable ${id.str}")) match {
//      case Class(_, vars, _, _) => vars.find(id).getOrElse(throw new Exception(s"Can not find var ${id.str}"))
//      case _ => throw new Exception(s"undefined variable ${id.str}")
//    }
//  }

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
        // todo:op
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
        // todo: map for false
        if (f.isDefined) {
          phi.addIncoming(f.get, falseBB)
        }
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
        val makeCall = (fname: Ident) => builder.createCall(getFun(fname), this.args(0) +: args.map(procExpr))
        val load = obj match
          case Expr.Symbol(sym) => {
            val klass = globalTable.classTable(sym)
            val method = klass.methods(target.str)
            makeCall(method.astNode.decl.name)
          }
          case _ => builder.createLoad(procExpr(obj))
        load.ty match
          case StructType(name, fields) => {
            currClass.methods.find(_.decl.name == target) match
              // todo: define default new
              // todo: 如果前面替换了param,这里如果是递归调用的话就有问题了
              case Some(value) => makeCall(value.decl.name)
              case None => ???
          }
          case PointerType(ty) => ???
          case _ => ???
      }
      case block: Expr.Block => procBlock(block)
      case Expr.Return(expr) => builder.createReturn(procExpr(expr))
      //      case Expr.Field(expr, ident) => ???
      //      case Self => ???
      //      case Expr.Constant(ident) => ???
      //      case Expr.Index(expr, i) => ???
      case _ => ???
      // todo:bad design
      if v.ty == InferType then v.withTy(expr.ty) else v
  }

  def makeType(tyInfo: TyInfo): Type = {
    tyInfo match
      // todo:fix this
      case TyInfo.Spec(ty) => Infer.translate(ty)
      case TyInfo.Nil => NilType
      case _ => InferType
  }

  def procStmt(stmt: ast.Stmt): Value = {
    stmt match
      // todo: bad design? init value
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