package rclang
package ast

import scala.util.parsing.input.Positional
import ast.Expr.Block
import ast.Ident

import ty.Typed

case class RcModule(items: List[Item]) extends ASTNode

enum Item extends ASTNode with Typed:
  case Method(decl: MethodDecl, body: Block) extends Item with Typed
  case Class(name: Ident, parent: Option[Ident], vars: List[FieldDef], methods:List[Method])

case class FieldDef(name: Ident, ty: TyInfo, initValue: Option[Expr]) extends ASTNode
case class Param(name: Ident, ty: TyInfo) extends ASTNode
case class Params(params: List[Param]) extends ASTNode
case class MethodDecl(name: Ident, inputs: Params, outType: TyInfo) extends ASTNode
