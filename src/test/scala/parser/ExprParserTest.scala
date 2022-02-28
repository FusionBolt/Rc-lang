package rclang
package parser

import org.scalatest.funspec.AnyFunSpec
import ast.{RcExpr, RcModule}
import ast.RcExpr.{Identifier, Number, Str}
import lexer.Token
import lexer.Token.{IDENTIFIER, NUMBER, STRING}

class ExprParserTest extends AnyFunSpec with ExprParser {
  def apply(tokens: Seq[Token]): Either[RcParserError, RcExpr] = {
    val reader = new RcTokenReader(tokens)
    expr(reader) match {
      case NoSuccess(msg, next) => Left(RcParserError(Location(next.pos.line, next.pos.column), msg))
      case Success(result, next) => Right(result)
    }
  }

  def expectSuccess(token: Token, expect: RcExpr): Unit = {
    apply(List(token)) match {
      case Left(value) => assert(false, value.msg)
      case Right(value) =>
    }
  }

  describe("number") {
    it ("succeed") {
      expectSuccess(NUMBER(3), Number(3))
    }
  }

  describe("identifier") {
    it("succeed") {
      expectSuccess(IDENTIFIER("foo"), Identifier("foo"))
    }
  }

  describe("str") {
    it("succeed") {
      expectSuccess(STRING("str"), Str("str"))
    }
  }
}
