package rclang
package ast

import scala.util.parsing.input.Positional
import ast.Expr.Block
import ast.Id

case class RcModule(items: List[Item]) extends Positional

enum Item extends Positional:
  case Method(decl: MethodDecl, body: Block)
  case Class(name: Id, parent: Option[Id], vars: List[FieldDef], methods:List[Method])

case class FieldDef(name: Id, ty: Type, initValue: Option[Expr]) extends Positional
case class MethodSignature() extends Positional
case class Param(name: Id, ty: Type) extends Positional
case class Params(params: List[Param]) extends Positional
case class MethodDecl(name: Id, inputs: Params, outType: Type) extends Positional
