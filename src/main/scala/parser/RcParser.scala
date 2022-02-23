package rclang
package parser

import lexer.RcToken.{IDENTIFIER, NUMBER, STRING}

import rclang.lexer.RcToken
import scala.util.parsing.combinator.Parsers
import scala.util.parsing.input.{NoPosition, Position, Reader}

class RcParser extends Parsers {
  override type Elem = RcToken

  protected def identifier: Parser[IDENTIFIER] = positioned {
    accept("identifier", { case id @ IDENTIFIER(name) => id })
  }

  protected def stringLiteral: Parser[STRING] = positioned {
    accept("string literal", { case lit @ STRING(name) => lit })
  }

  protected def number: Parser[NUMBER] = positioned {
    accept("number literal", { case num @ NUMBER(name) => num })
  }

  class RcTokenReader(tokens: Seq[RcToken]) extends Reader[RcToken] {
    override def first: RcToken = tokens.head

    override def atEnd: Boolean = tokens.isEmpty

    override def pos: Position = tokens.headOption.map(_.pos).getOrElse(NoPosition)

    override def rest: Reader[RcToken] = new RcTokenReader(tokens.tail)
  }
}