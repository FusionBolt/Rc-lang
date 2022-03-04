package rclang
package lexer

import org.scalatest.funspec.AnyFunSpec
import lexer.Lexer
import lexer.Token.*

import org.scalatest.funspec.AnyFunSpec

class LexerTest extends AnyFunSpec {
  def singleToken(tokens: List[Token]): Token = {
    assert(tokens.size == 1, tokens)
    tokens.last
  }

  def expectSuccess(str: String, token: Token) = {
    Lexer(str) match {
      case Left(value) => assert(false, value.msg)
      case Right(value) => assert(singleToken(value) == token)
    }
  }

  def expectSuccess(str: String, tokens: List[Token]) = {
    Lexer(str) match {
      case Left(value) => assert(false, value.msg)
      case Right(value) => assert(value == tokens)
    }
  }

  def expectFailed(str: String) = {
    Lexer(str) match {
      case Left(value) =>
      case Right(value) => assert(false, singleToken(value))
    }
  }

  def expectNotEql(str: String, token: Token) = {
    Lexer(str) match {
      case Left(value) => assert(false, value.msg)
      case Right(value) => assert(singleToken(value) != token)
    }
  }

  describe("number") {
    it("succeed") {
      expectSuccess("123", NUMBER(123))
    }
  }

  describe("bool") {
    it("succeed") {
      expectSuccess("true", TRUE)
      expectSuccess("false", FALSE)
    }
  }

  describe("identifier") {
    it("succeed") {
      expectSuccess("foo", IDENTIFIER("foo"))
      expectSuccess("foo1", IDENTIFIER("foo1"))
      expectFailed("1foo")
    }
  }

  def expectKeywordNotId(str: String): Unit = {
    expectNotEql(str, IDENTIFIER(str))
  }

  describe("keyword is not a id") {
    it("succeed") {
      val keywords = List("true", "false", "def", "end", "if", "elsif", "else", "while", "class", "super")
      keywords.map(expectKeywordNotId)
    }
  }

  describe("string") {
    it("succeed") {
      expectSuccess("\"describe str\"", STRING("describe str"))
      // todo:true error info
      expectFailed("\"describe str")
    }
  }

  describe ("eol") {
    it("succeed") {
      expectSuccess("id \n id", List(IDENTIFIER("id"), EOL, IDENTIFIER("id")))
    }
  }

  describe("") {
    it("succeed") {
      expectSuccess("true false", List(TRUE, FALSE))
    }
  }

  describe("operator") {
    it("succeed") {
      def expectOp(op: Char) = expectSuccess(op.toString, OPERATOR(op.toString))

      "+-*/%^~!<>".foreach(expectOp)
    }
  }

  describe("fun") {
    it("succeed") {
      val src = """def main end"""
      expectSuccess(src, List(DEF, IDENTIFIER("main"), END))
    }
  }

  describe("space") {
    it("succeed") {
      expectSuccess(""" def """, List(DEF))
    }
  }
}