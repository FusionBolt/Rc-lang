package rclang
package parser

import org.scalatest.funspec.AnyFunSpec
import ast.{Expr, RcModule}
import ast.Expr.{Identifier, Number, Str}
import ast.BoolConst
import lexer.Token
import lexer.Token.{IDENTIFIER, NUMBER, STRING, TRUE, FALSE, RETURN}

class ExprParserTest extends AnyFunSpec with ExprParser {
  def apply(tokens: Seq[Token]): Either[RcParserError, Expr] = {
    doParser(tokens, expr)
  }

  def expectSuccess(token: Token, expect: Expr): Unit = {
    expectSuccess(List(token), expect)
  }

  def expectSuccess(tokens: Seq[Token], expect: Expr): Unit = {
    apply(tokens) match {
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

  describe("bool") {
    it("succeed") {
      expectSuccess(TRUE, Expr.Bool(BoolConst.True))
      expectSuccess(FALSE, Expr.Bool(BoolConst.False))
    }
  }

  describe("str") {
    it("succeed") {
      expectSuccess(STRING("str"), Str("str"))
    }
  }
}
