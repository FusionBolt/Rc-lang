package rclang
package parser

import rclang.ast.*
import rclang.lexer._
import rclang.lexer.RcToken._

import scala.util.parsing.combinator.Parsers
import scala.util.parsing.input.{NoPosition, Position, Reader}

object RcParser extends Parsers {
  override type Elem = RcToken

  class RcTokenReader(tokens: Seq[RcToken]) extends Reader[RcToken] {
    override def first: RcToken = tokens.head

    override def atEnd: Boolean = tokens.isEmpty

    override def pos: Position = tokens.headOption.map(_.pos).getOrElse(NoPosition)

    override def rest: Reader[RcToken] = new RcTokenReader(tokens.tail)
  }

  def apply(tokens: Seq[RcToken]): Either[RcParserError, RcAST] = {
    val reader = new RcTokenReader(tokens)
    program(reader) match {
      case NoSuccess(msg, next) => Left(RcParserError(Location(next.pos.line, next.pos.column), msg))
      case Success(result, next) => Right(result)
    }
  }

  def program: Parser[RcAST] = positioned {
    phrase(expr) ^^ (expr => RcAST.Expr(expr))
  }

  def expr: Parser[RcExpr] = positioned {
    bool
      | identifier ^^ { case IDENTIFIER(id) => RcExpr.Identifier(id) }
      | stringLiteral ^^ { case STRING(str) => RcExpr.Str(str) }
      | number ^^ { case NUMBER(int) => RcExpr.Number(int) }
  }

  def bool: Parser[RcExpr] = positioned {
    TRUE ^^ (_ => RcExpr.Bool(BoolConst.True)) |
      FALSE ^^ (_ => RcExpr.Bool(BoolConst.False))
  }

  def method: Parser[RcDefine] = positioned {
    DEF ~ identifier ~ END ^^ (_ => RcDefine.Method)
  }

  private def identifier: Parser[IDENTIFIER] = positioned {
    accept("identifier", { case id @ IDENTIFIER(name) => id })
  }

  private def stringLiteral: Parser[STRING] = positioned {
    accept("string literal", { case lit @ STRING(name) => lit })
  }

  private def number: Parser[NUMBER] = positioned {
    accept("number literal", { case num @ NUMBER(name) => num })
  }
}