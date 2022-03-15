package rclang
package ast

import scala.util.parsing.input.Positional

enum Type extends Positional:
  case Spec(ty: Id)
  case Infer
  case Nil