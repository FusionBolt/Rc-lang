package rclang
package parser

import rclang.ast.*
import rclang.lexer._
import rclang.lexer.RcToken._

import scala.util.parsing.combinator.Parsers
import scala.util.parsing.input.{NoPosition, Position, Reader}

object ValueParser extends Parsers {
  def identifier: Parser[IDENTIFIER] = positioned {
    accept("identifier", { case id @ IDENTIFIER(name) => id })
  }

  def stringLiteral: Parser[STRING] = positioned {
    accept("string literal", { case lit @ STRING(name) => lit })
  }

  def number: Parser[NUMBER] = positioned {
    accept("number literal", { case num @ NUMBER(name) => num })
  }
}

object StatementParser extends Parsers {
  def local: Parser[Statement.Local] = positioned {
    VAR ~ ValueParser.identifier ~ EQL ^^ {
      case _ ~ IDENTIFIER(id) ~ _ => Statement.Local(id, Type.Nil)
    }
  }
}

object ExprParser extends Parsers {

}

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
    phrase(define ^^ (defs => RcAST.Define(defs)) | expr ^^ (exprs => RcAST.Expr(exprs)))
  }

  def define: Parser[RcItem] = method

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

  def args: Parser[Params] = positioned {
    LEFT_PARENT_THESES ~ repsep(identifier, COMMA) ~ RIGHT_PARENT_THESES ^^ {
      case _ ~ params ~ _ => Params(params.map(x => Param(x.str)))
    }
  }

  def method: Parser[RcItem] = positioned {
    DEF ~ identifier ~ args ~ END ^^ {
      case _ ~ IDENTIFIER(id) ~ args ~ _ => RcItem.Method(MethodDecl(id, args, "output"), List())
    }
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