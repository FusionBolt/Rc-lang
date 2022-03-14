package rclang
package ast

import scala.language.implicitConversions
import scala.util.parsing.input.Positional

case class Id(id: String) extends Positional
implicit def strToId(str: String): Id = Id(str)

enum AST extends Positional:
  case Modules(modules: List[RcModule])