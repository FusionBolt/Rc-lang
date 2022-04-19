package rclang
package ast

import scala.language.implicitConversions
import scala.util.parsing.input.Positional

case class Ident(str: String) extends Positional

object ImplicitConversions {
  implicit def strToId(str: String): Ident = Ident(str)
  implicit def IdToStr(id: Ident): String = id.str
  implicit def boolToAST(b: Boolean): Expr.Bool = Expr.Bool(b)
  implicit def intToAST(i: Int): Expr.Number = Expr.Number(i)
}

trait ASTNode extends Positional

case class Modules(modules: List[RcModule]) extends ASTNode
object Empty extends ASTNode