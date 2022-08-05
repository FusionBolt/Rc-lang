package rclang
package mir
import ast.*

import mir.IRBuilder
import ty.*

// todo:不同层的id怎么处理
// todo:debug info
case class ToMIR(var globalTable: Map[Ident, Item] = Map[Ident, Item]()) {
  var module = Module()
  def proc(rcModule: RcModule): Module = {
    module.name = rcModule.name
    rcModule.items.foreach(_ match
      case m: Method => getFn(m)
      case c: Class => procClass(c)
      case _ => throw new Exception("not support"))
    module
  }

  def procClass(klass: Class) = {
    // todo:rename fn, how to distinct between normal name and link name
    var fns = klass.methods.map(getFn(_, klass.name.str)).map(_.fnType)
    var ty = StructType(klass.name.str, Map())
    module.types += ty
  }

  def getFn(fn: Method, prefix: String = ""): Function = FnToMIR(globalTable, module).procMethod(fn, prefix)
}

case class FnToMIR(var globalTable: Map[Ident, Item], var parentModule: Module) {
  def getFun(id: Ident, prefix: String = ""): Function = {
    parentModule.fnTable.getOrElse(id.str, globalTable(id) match {
      case f: Method => {
        val fn = FnToMIR(globalTable, parentModule).procMethod(f, prefix)
        parentModule.fnTable += (id.str -> fn)
        fn
      }
      case _ => ???
    })
  }

  var builder = IRBuilder()
  var env = Map[Ident, Value]()

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
    val args = procArgument(method.decl.inputs)
    env = args.map(arg => Ident(arg.name) -> Integer(1)).toMap
    val fnName = s"${prefix}_${method.decl.name.str}"
    val fn = Function(fnName, makeType(method.decl.outType), args)
    parentModule.fnTable += (fnName -> fn)
    builder = IRBuilder()
    builder.currentFn = fn
    procBlock(method.body)
    if(!builder.currentBasicBlock.stmts.last.isInstanceOf[Return]) {
      builder.createReturn(builder.currentBasicBlock.stmts.last)
    }
    fn.bbs = builder.basicBlocks
    fn.entry = builder.basicBlocks.head
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
  def lookup(id: Ident): Value = {
    env(id)
  }

  def procExpr(expr: Expr): Value = {
    val v = expr match
      case Expr.Number(v) => Integer(v)
      case Expr.Identifier(ident) => builder.createLoad(env(ident))
      case Expr.Bool(b) => Bool(b)
      case Expr.Binary(op, lhs, rhs) => {
        val l = procExpr(lhs)
        val r = procExpr(rhs)
        // todo:op
        builder.createBinary(op.toString, l, r)
      }
      case Expr.Str(str) => Str(str)
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
      case Expr.Call(target, args) => builder.createCall(getFun(target), args.map(procExpr))
      //      case Expr.MethodCall(obj, target, args) => ???
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
      case ast.Stmt.While(cond, body) => ???
      case ast.Stmt.Assign(name, value) => builder.createStore(procExpr(value), env(name))
  }
}