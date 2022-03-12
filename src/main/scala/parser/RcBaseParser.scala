package rclang
package parser

import lexer.Token.*

import rclang.lexer.Token

import scala.util.parsing.combinator.Parsers
import scala.util.parsing.input.{CharSequenceReader, NoPosition, Position, Positional, Reader}

trait RcBaseParser extends Parsers {
  override type Elem = Token
  protected def identifier: Parser[IDENTIFIER] = positioned {
    accept("identifier", { case id @ IDENTIFIER(name) => id })
  }

  protected def stringLiteral: Parser[STRING] = positioned {
    accept("string literal", { case lit @ STRING(str) => lit })
  }

  protected def number: Parser[NUMBER] = positioned {
    accept("number literal", { case num @ NUMBER(n) => num })
  }

  protected def operator: Parser[OPERATOR] = positioned {
    accept("operator", { case op @ OPERATOR(_) => op })
  }

  protected def oneline[T](p: Parser[T]): Parser[T] = log(p <~ EOL.+)("oneline")
  protected def onelineOpt[T](p: Parser[T]): Parser[T] = log(p <~ EOL.*)("oneline")

  protected def nextline[T](p: Parser[T]): Parser[T] = log(EOL.+ ~> p)("nextline")

  protected def makeParserError(next: Input, msg: String) = RcParserError(Location(next.pos.line, next.pos.column), msg)

  protected def doParser[T](tokens: Seq[Token], parser: Parser[T]): Either[RcParserError, T] = {
    doParserImpl(tokens, parser).map(_._1)
  }

  protected def doParserImpl[T](tokens: Seq[Token], parser: Parser[T]): Either[RcParserError, (T, Input)] = {
    val reader = new RcTokenReader(tokens)
    parser(reader) match {
      case NoSuccess(msg, next) => Left(makeParserError(next, msg))
      case Success(result, next) => Right(result, next)
    }
  }

  def noEmptyEval[T](l: List[T], f: List[T] => List[T], els: List[T] = List()) = if l.isEmpty then els else f(l)

  class RcTokenReader(tokens: Seq[Token]) extends Reader[Token] {
    override def first: Token = tokens.head

    override def atEnd: Boolean = tokens.isEmpty

    override def pos: Position = tokens.headOption.map(_.pos).getOrElse(NoPosition)

    override def rest: Reader[Token] = new RcTokenReader(tokens.tail)

    override def toString: String = {
      val c = if (atEnd) "" else s"'$first', ..."
      s"RcTokenReader($c)"
    }
  }
}