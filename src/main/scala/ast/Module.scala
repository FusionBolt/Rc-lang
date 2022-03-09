package rclang
package ast

import scala.util.parsing.input.Positional
import ast.Expr.Block

case class RcModule(methods: List[Item]) extends Positional

enum Item extends Positional:
  case Method(decl: MethodDecl, body: Block)
  case Class

case class MethodSignature() extends Positional
case class Param(name: Id) extends Positional
case class Params(params: List[Param]) extends Positional
case class MethodDecl(name: Id, inputs: Params, output: Type) extends Positional

