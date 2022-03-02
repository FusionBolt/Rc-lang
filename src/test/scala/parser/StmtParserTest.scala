package rclang
package parser

import org.scalatest.funspec.AnyFunSpec
import rclang.ast.*
import rclang.lexer.Token.*
import rclang.lexer.Token

class StmtParserTest extends AnyFunSpec with StmtParser {
  def apply(tokens: Seq[Token]): Either[RcParserError, Stmt] = {
    doParser(tokens, statement)
  }

  def expectSuccess(token: Seq[Token], expect: Stmt): Unit = {
    apply(token) match {
      case Left(value) => assert(false, value.msg)
      case Right(value) => assert(value == expect)
    }
  }

  describe("local") {
    it("succeed") {
      expectSuccess(List(VAR, IDENTIFIER("a"), EQL, NUMBER(1)), Stmt.Local("a", Type.Nil, Expr.Number(1)))
    }
  }

  describe("expr") {
    it("succeed") {
      expectSuccess(List(NUMBER(1)), Stmt.Expr(Expr.Number(1)))
    }
  }

  describe("return") {
    it("succeed") {
      expectSuccess(List(RETURN, NUMBER(1)), Stmt.Return(Expr.Number(1)))
    }
  }
}