package rclang
package parser

import lexer.Token.{IDENTIFIER, NUMBER, STRING}

import rclang.lexer.Token
import scala.util.parsing.combinator.Parsers
import scala.util.parsing.input.{NoPosition, Position, Reader}

trait RcBaseParser extends Parsers {
  override type Elem = Token

  protected def identifier: Parser[IDENTIFIER] = positioned {
    accept("identifier", { case id @ IDENTIFIER(name) => id })
  }

  protected def stringLiteral: Parser[STRING] = positioned {
    accept("string literal", { case lit @ STRING(name) => lit })
  }

  protected def number: Parser[NUMBER] = positioned {
    accept("number literal", { case num @ NUMBER(name) => num })
  }

  class RcTokenReader(tokens: Seq[Token]) extends Reader[Token] {
    override def first: Token = tokens.head

    override def atEnd: Boolean = tokens.isEmpty

    override def pos: Position = tokens.headOption.map(_.pos).getOrElse(NoPosition)

    override def rest: Reader[Token] = new RcTokenReader(tokens.tail)
  }
}