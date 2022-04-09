package rclang
package ast

import scala.util.parsing.input.Positional
import ast.Expr.Block
import ast.Ident

case class RcModule(items: List[Item]) extends Positional

enum Item extends Positional:
  case Method(decl: MethodDecl, body: Block)
  case Class(name: Ident, parent: Option[Ident], vars: List[FieldDef], methods:List[Method])

case class FieldDef(name: Ident, ty: Type, initValue: Option[Expr]) extends Positional
case class MethodSignature() extends Positional
case class Param(name: Ident, ty: Type) extends Positional
case class Params(params: List[Param]) extends Positional
case class MethodDecl(name: Ident, inputs: Params, outType: Type) extends Positional
