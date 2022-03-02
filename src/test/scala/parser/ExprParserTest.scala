package rclang
package parser

import org.scalatest.funspec.AnyFunSpec

import ast.*
import ast.Expr.{Identifier, Number, Str}
import lexer.Token
import lexer.Token.*
import scala.language.postfixOps
import org.scalatest._

class ExprParserTest extends ExprParser with BaseParserTest {
  def apply(tokens: Seq[Token]): Either[RcParserError, Expr] = {
    doParser(tokens, expr)
  }

  def trueExpr = Expr.Bool(true)
  def falseExpr = Expr.Bool(false)

  def expectSuccess(token: Token, expect: Expr): Unit = {
    expectSuccess(List(token), expect)
  }

  def expectSuccess(tokens: Seq[Token], expect: Expr): Unit = {
    apply(tokens) match {
      case Left(value) => assert(false, value.msg)
      case Right(value) => (value should equal (expect))
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
      expectSuccess(TRUE, Expr.Bool(true))
      expectSuccess(FALSE, Expr.Bool(false))
    }
  }

  describe("str") {
    it("succeed") {
      expectSuccess(STRING("str"), Str("str"))
    }
  }

  describe("if") {
    it("full succeed") {
      expectSuccess(List(IF, TRUE, NUMBER(1), ELSIF, FALSE, NUMBER(2), ELSE, NUMBER(3)),
        Expr.If(trueExpr, Number(1), List(Elsif(falseExpr, Number(2))), Some(Number(3))))
    }

    it("no elsif") {
      expectSuccess(List(IF, TRUE, NUMBER(1), ELSE, NUMBER(3)),
        Expr.If(trueExpr, Number(1), List(), Some(Number(3))))
    }

    it("no else") {
      expectSuccess(List(IF, TRUE, NUMBER(1), ELSIF, FALSE, NUMBER(2)),
        Expr.If(trueExpr, Number(1), List(Elsif(falseExpr, Number(2))), None))
    }
  }

  describe("call") {
    it("empty args") {
      expectSuccess(List(IDENTIFIER("foo"), LEFT_PARENT_THESES, RIGHT_PARENT_THESES), Expr.Call("foo", List()))
    }

    it("multi args") {
      expectSuccess(List(IDENTIFIER("foo"), LEFT_PARENT_THESES, NUMBER(1), COMMA, NUMBER(2), RIGHT_PARENT_THESES), Expr.Call("foo", List(Number(1), Number(2))))
    }
  }
}
