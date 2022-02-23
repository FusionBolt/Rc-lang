package rclang
package parser

import org.scalatest.funspec.AnyFunSpec
import lexer.RcToken
import lexer.RcToken.*

import rclang.ast.{Params, RcAST, RcExpr, RcItem}
import rclang.ast.RcAST.Expr.*
import rclang.ast.RcExpr.*
import rclang.ast.*

class ParserTest extends AnyFunSpec {
  def expectSuccess(token: RcToken, expect: RcExpr): Unit = {
    ModuleParser(List(token)) match {
      case Left(value) => assert(false, value.msg)
      case Right(value) =>
    }
  }

  def expectSuccess(token: List[RcToken], expect: RcItem): Unit = {
    ModuleParser(token) match {
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

  def makeMethod(name: String) = {
    List(DEF, IDENTIFIER("foo"), LEFT_PARENT_THESES, RIGHT_PARENT_THESES, END)
  }

  describe("fun") {
    it("empty") {
      expectSuccess(makeMethod("foo"),
        RcItem.Method(MethodDecl("foo", Params(List()), Type.Nil), Block(List())))
    }
  }
}