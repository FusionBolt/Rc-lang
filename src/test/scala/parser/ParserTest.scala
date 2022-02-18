package rclang
package parser

import org.scalatest.funsuite.AnyFunSuite
import lexer.RcToken
import lexer.RcToken.*

import rclang.ast.{RcAST, RcExpr}
import rclang.ast.RcAST.Expr.*
import rclang.ast.RcExpr.*

class ParserTest extends AnyFunSuite {
  def expectSuccess(token: RcToken, expect: RcExpr): Unit = {
    RcParser(List(token)) match {
      case Left(value) => assert(false, value.msg)
      case Right(value) =>
    }
  }

  test("number") {
    expectSuccess(NUMBER(3), Number(3))
  }

  test("identifier") {
    expectSuccess(IDENTIFIER("foo"), Identifier("foo"))
  }

  test("str") {
    expectSuccess(STRING("str"), Str("str"))
  }
}