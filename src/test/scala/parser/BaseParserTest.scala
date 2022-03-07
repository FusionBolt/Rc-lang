package rclang
package parser

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should._
import lexer.Token

trait BaseParserTest extends AnyFunSpec with RcBaseParser with Matchers {
//  def expectSuccess[T](token: Seq[Token], expect: T) = {
//    apply(token) match {
//      case Left(value) => assert(false, value.msg)
//      case Right(value) => assert(value == expect)
//    }
//  }
  def noEmptyEval[T](l: List[T], f: List[T] => List[T], els: List[T] = List()) = if l.isEmpty then els else f(l)
}
