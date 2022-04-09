package rclang
package ast

import scala.language.implicitConversions
import scala.util.parsing.input.Positional

case class Ident(str: String) extends Positional

object ImplicitConversions {
  implicit def strToId(str: String): Ident = Ident(str)
  implicit def IdToStr(id: Ident): String = id.str
}

trait ASTNode extends Positional

case class Modules(modules: List[RcModule]) extends ASTNode
object Empty extends ASTNode