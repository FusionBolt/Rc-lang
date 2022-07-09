package rclang
package ast

import ast.*
import ast.Expr.*
import ast.ImplicitConversions.*

trait ASTBuilder {
  def mkASTField(name: String, ty: String) = FieldDef(name, TyInfo.Spec(ty), None)
  def mkASTClass(name: String) = Class(name, None, List(), List())
  def mkASTClass(name: String, parent: String) = Class(name, Some(parent), List(), List())
  def mkASTClass(name: String, method: Method) = Class(name, None, List(), List(method))
  def mkASTClass(name: String, field: FieldDef) = Class(name, None, List(field), List())
  def mkASTClass(name: String, field: FieldDef, method: Method) = Class(name, None, List(field), List(method))
  def makeExprBlock(cond: Expr): Block = Block(List(Stmt.Expr(cond)))
  def makeIf(cond: Expr, thenExpr: Expr, elseExpr: Expr) = If(cond, makeExprBlock(thenExpr), Some(elseExpr))
  def makeLastIf(cond: Expr, thenExpr: Expr, elseExpr: Expr) = If(cond, makeExprBlock(thenExpr), Some(makeExprBlock(elseExpr)))
  def makeIf(cond: Expr, thenExpr: Expr, elseExpr: Option[Expr]) = If(cond, makeExprBlock(thenExpr), elseExpr)
  def mkASTMemField(name: String, field: String) = Expr.Field(Expr.Identifier(name), field)
  def mkASTMemCall(name: String, field: String, args: List[Expr] = List()) =
    Expr.MethodCall(Expr.Identifier(name), field, List())
  def makeStmtBlock(cond: Stmt): Block = Block(List(cond))
  def makeLocal(name: String, value: Expr): Stmt.Local = Stmt.Local(name, TyInfo.Infer, value)
  def makeASTMethod(name: String,
                    params: List[Param] = List(),
                    retType:TyInfo = TyInfo.Nil,
                    block: List[Stmt] = List()): Method = {
    Method(MethodDecl(name, Params(params), retType), Block(block))
  }
  
  def mkFnInMod(name: String,
                params: List[Param] = List(),
                retType:TyInfo = TyInfo.Nil,
                block: List[Stmt] = List()) = RcModule(List(makeASTMethod(name, params, retType, block)))
}
