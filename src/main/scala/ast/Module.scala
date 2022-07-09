package rclang
package ast

import scala.util.parsing.input.Positional
import ast.Expr.Block
import ast.Ident

import ty.Typed

case class RcModule(items: List[Item], name: String = "") extends ASTNode {
  override def toString: String = s"RcModule:$name\n" + items.mkString("\n")
}

sealed class Item extends ASTNode with Typed

case class Method(decl: MethodDecl, body: Block) extends Item{
  override def toString: String = s"Method:${decl.name}\n${body.toString}"
}

case class Class(name: Ident, parent: Option[Ident], vars: List[FieldDef], methods:List[Method]) extends Item

case class FieldDef(name: Ident, ty: TyInfo, initValue: Option[Expr]) extends ASTNode
case class Param(name: Ident, ty: TyInfo) extends ASTNode
case class Params(params: List[Param]) extends ASTNode
case class MethodDecl(name: Ident, inputs: Params, outType: TyInfo) extends ASTNode
