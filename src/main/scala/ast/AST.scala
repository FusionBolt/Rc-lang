package rclang
package ast

import scala.language.implicitConversions
import scala.util.parsing.input.Positional

case class Id(id: String) extends Positional
implicit def strToId(str: String): Id = Id(str)
implicit def IdToStr(id: Id): String = id.id

trait ASTNode extends Positional {

}

case class Modules(modules: List[RcModule]) extends ASTNode
case class Empty() extends ASTNode