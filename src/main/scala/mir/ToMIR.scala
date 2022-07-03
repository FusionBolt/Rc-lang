package rclang
package mir
import ast.*

import mir.IRBuilder
import ty.*

// todo:不同层的id怎么处理
// todo:debug info
case class ToMIR(table: Map[Ident, Item] = Map[Ident, Item]()) {
  var builder = IRBuilder()
  var env = Map[Ident, Value]()
  var globalTable = table
  var fnTable = Map[Ident, Function]()
  def proc(module: RcModule): List[Function] = {
    module.items.map(_ match
      case m: Item.Method => procMethod(m)
      case _ => ???)
  }

  def getFun(id: Ident): Function = {
    fnTable.getOrElse(id, globalTable(id) match {
      case f: Item.Method => {
        val fn = ToMIR(globalTable).procMethod(f)
        fnTable += (id -> fn)
        fn
      }
      case _ => ???
    })

  }

  // todo: argument and params
  def procArgument(params: Params): List[Argument] = {
    params.params.map(param => {
      val ty = param.ty
      val name = param.name
      Argument(name.str, makeType(ty))
    })
  }

  // todo：only call for a new instance, make a function builder
  def procMethod(method: Item.Method): Function = {
    // ir builder manages the function
    val args = procArgument(method.decl.inputs)
    val fn = Function(method.decl.name.str, args)
    builder = IRBuilder()
    builder.currentFn = fn
    procBlock(method.body)
    fn.bbs = builder.basicBlocks
    fn.entry = builder.basicBlocks.head
    fn
  }

  def procBlock(block: Expr.Block): Value = {
    builder.insertBasicBlock()
    block.stmts.map(procStmt).last
  }

  def procExpr(expr: Expr): Value = {
    expr match
      case Expr.Number(v) => Constant.Integer(v)
      case Expr.Identifier(ident) => builder.createLoad(env(ident))
      case Expr.Bool(b) => Constant.Bool(b)
      case Expr.Binary(op, lhs, rhs) => {
        val l = procExpr(lhs)
        val r = procExpr(rhs)
        // todo:op
        builder.createBinary(op.toString, l, r)
      }
      case Expr.Str(str) => Constant.Str(str)
      case Expr.If(cond, true_branch, false_branch) => {
        val c = procExpr(cond)
        val trueBB = BasicBlock()
        val falseBB = BasicBlock()
        val mergeBB = BasicBlock()
        builder.createCondBr(c, trueBB, falseBB)
        builder.insertBasicBlock(trueBB)
        val t = procExpr(true_branch)
        builder.createBr(mergeBB)
        builder.insertBasicBlock(falseBB)
        val f = false_branch match
          case Some(value) => procExpr(value)
          case None => null
        builder.createBr(mergeBB)
        builder.insertBasicBlock(mergeBB)
        val phi = builder.createPHINode()
        phi.addIncoming(t, trueBB)
        phi.addIncoming(f, falseBB)
// todo:fix this
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
  }

  def makeType(tyInfo: TyInfo): Type = {
    tyInfo match
      // todo:fix this
      case TyInfo.Spec(ty) => Infer.translate(ty)
      case TyInfo.Nil => Type.Nil
      case _ => ???
  }

  def procStmt(stmt: ast.Stmt): Value = {
    stmt match
      case ast.Stmt.Local(name, tyInfo, value) => {
        env += name -> procExpr(value)
        builder.createAlloc(name.str, makeType(tyInfo))
      }
      case ast.Stmt.Expr(expr) => procExpr(expr)
      case ast.Stmt.While(cond, body) => ???
      case ast.Stmt.Assign(name, value) => builder.createStore(procExpr(value), env(name))
  }
}
