package rclang
package ast

import scala.util.parsing.input.Positional

enum Type extends ASTNode:
  case Spec(ty: Id)
  case Infer
  case Nil