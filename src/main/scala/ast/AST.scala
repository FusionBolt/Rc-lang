package rclang
package ast

import scala.util.parsing.input.Positional

type Id = String

enum AST extends Positional:
  case Modules(modules: List[RcModule])