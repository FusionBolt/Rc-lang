package rclang
package ty

import ast.{ASTNode, ASTVisitor, Expr, FieldDef, Ident, Item, MethodDecl, Modules, Param, RcModule, Stmt, TyInfo}
import ast.Expr.*
import ast.Stmt.*
import ast.*

import scala.collection.immutable.Map

case object TypedTranslator {
  var tyCtxt: TyCtxt = TyCtxt()
  def apply(tyCtxt: TyCtxt)(module: RcModule): RcModule = {
    this.tyCtxt = tyCtxt
    // update local table in TypedTranslator will cause Infer ctxt update
    // because of pass a typCtxt by Ref
    Infer.enter(tyCtxt, RcModuleTrans(module))
  }

  // fn type
  def RcModuleTrans(module: RcModule): RcModule = {
    val items = module.items.map(itemTrans)
    RcModule(items)
  }

  def itemTrans(item: Item): Item = {
    item match
      case m: Method => tyCtxt.enter(methodTrans(m))
      case c: Class => tyCtxt.enter(classTrans(c))
  }

  def classTrans(klass: Class): Class = {
    val methods = klass.methods.map(tyCtxt.enter(methodTrans))
    klass.copy(methods = methods)
  }

  def exprTrans(expr: Expr): Expr =
    (expr match
      case Binary(op, lhs, rhs) => Binary(op, lhs.withInfer, rhs.withInfer)
      case If(cond, true_branch, false_branch) => {
        val false_br = false_branch match
          case Some(fBr) => Some(fBr.withInfer)
          case None => None
        If(cond.withInfer,
          true_branch.withInfer.asInstanceOf[Block],
          false_br)
      }
      case Call(target, args) => Call(target, args.map(_.withInfer))
      case Return(expr) => Return(expr.withInfer)
      case Lambda(args, block) => ???
      // todo: TyCtxt.Enter and Infer.Enter
      case MethodCall(obj, target, args) => ???
      case Block(stmts) => tyCtxt.enter(Block(stmts.map(stmtTrans)))
      case Field(expr, ident) => ???
      case Self => ???
      case Constant(ident) => ???
      case Index(expr, i) => ???
      case _ => expr).withInfer

  def stmtTrans(stmt: Stmt): Stmt =
    (stmt match
      case Local(name, ty, value) =>
        val localTy = ty match
          case TyInfo.Spec(_) => Infer.translate(ty)
          case _ => Infer(value)
        tyCtxt.addLocal(name, localTy)
        // todo: how to process if ty is Infer
        Local(name, ty, value.withInfer).withTy(localTy)
      case Stmt.Expr(expr) => Stmt.Expr(expr.withInfer)
      case While(cond, body) => While(cond.withInfer, body.withInfer)
      case Assign(name, value) => Assign(name, value.withInfer))
    .withInfer

  def methodTrans(method: Method): Method = {
    method.copy(body = exprTrans(method.body).asInstanceOf[Block]).withInfer
  }
}