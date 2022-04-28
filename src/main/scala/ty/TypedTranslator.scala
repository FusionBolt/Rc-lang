package rclang
package ty

import ast.{ASTVisitor, Expr, FieldDef, Ident, Item, MethodDecl, Modules, Param, RcModule, Stmt, TyInfo}
import ast.Expr.*
import ast.Stmt.*

case object TypedTranslator {
  def RcModuleTrans(module: RcModule): RcModule = {
    RcModule(module.items.map(itemTrans))
  }

  def itemTrans(item: Item): Item = {
    item match
      case m: Item.Method => methodTrans(m)
      case c: Item.Class => ???
  }

  def exprTrans(expr: Expr): Expr =
    (expr match
      case Identifier(ident) => ???
      case Binary(op, lhs, rhs) => Binary(op, lhs.withInfer, rhs.withInfer)
      case If(cond, true_branch, false_branch) => {
        val f = false_branch match
          case Some(fBr) => Some(fBr.withInfer)
          case None => None
        If(cond.withInfer,
          true_branch.withInfer.asInstanceOf[Block],
          f)
      }
      case Call(target, args) => Call(target, args.map(_.withInfer))
      case Return(expr) => Return(expr.withInfer)
      case Lambda(args, block) => ???
      case MethodCall(obj, target, args) => ???
      case Block(stmts) => Block(stmts.map(stmtTrans))
      case Field(expr, ident) => ???
      case Self => ???
      case Constant(ident) => ???
      case Index(expr, i) => ???
      case _ => expr).withInfer

  def stmtTrans(stmt: Stmt): Stmt =
    (stmt match
      case Local(name, ty, value) => Local(name, ty, value.withInfer)
      case Stmt.Expr(expr) => Stmt.Expr(expr.withInfer)
      case While(cond, body) => While(cond.withInfer, body.withInfer)
      case Assign(name, value) => Assign(name, value.withInfer))
    .withInfer

//
//  def tyInfoTrans(ty: TyInfo): TyInfo = {
//
//  }
//
//  def methodDeclTrans(decl: MethodDecl): MethodDecl = {
//
//  }
//
//  def paramTrans(param: Param): Param = {
//
//  }
//
//  def fieldDefTrans(field: FieldDef): FieldDef = {
//
//  }
//
  def methodTrans(method: Item.Method): Item = {
    method.copy(body = method.body.withInfer)
  }

//  def classTrans(klass: Item.Class): Item = {
//  }
}