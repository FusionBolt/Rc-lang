package rclang
package ast

import scala.util.parsing.input.Positional

enum TyInfo extends ASTNode:
  case Spec(ty: Ident)
  case Infer
  case Nil