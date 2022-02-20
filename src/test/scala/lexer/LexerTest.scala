package rclang
package lexer

import org.scalatest.funsuite.AnyFunSuite
import lexer.RcLexer
import lexer.RcToken.*

import org.scalatest.funspec.AnyFunSpec

class TT extends AnyFunSpec {
  describe("test") {
    it("shoule be zero") {

    }

    describe("f") {

    }
    it ("test") {

    }
  }
}

class LexerTest extends AnyFunSuite {
  def singleToken(tokens: List[RcToken]): RcToken = {
    assert(tokens.size == 1, tokens)
    tokens.last
  }

  def expectSuccess(str: String, token: RcToken) = {
    RcLexer(str) match {
      case Left(value) => assert(false, value.msg)
      case Right(value) => assert(singleToken(value) == token)
    }
  }

  def expectSuccess(str: String, tokens: List[RcToken]) = {
    RcLexer(str) match {
      case Left(value) => assert(false, value.msg)
      case Right(value) => assert(value == tokens)
    }
  }

  def expectFailed(str: String) = {
    RcLexer(str) match {
      case Left(value) =>
      case Right(value) => assert(false, singleToken(value))
    }
  }

  def expectNotEql(str: String, token: RcToken) = {
    RcLexer(str) match {
      case Left(value) => assert(false, value.msg)
      case Right(value) => assert(singleToken(value) != token)
    }
  }

  test("number") {
    expectSuccess("123", NUMBER(123))
  }

  test("bool") {
    expectSuccess("true", TRUE)
    expectSuccess("false", FALSE)
  }

  test("identifier") {
    expectSuccess("foo", IDENTIFIER("foo"))
    expectSuccess("foo1", IDENTIFIER("foo1"))
    expectFailed("1foo")
  }

  def expectKeywordNotId(str: String): Unit = {
    expectNotEql(str, IDENTIFIER(str))
  }

  test("keyword is not a id") {
    val keywords = List("true", "false", "def", "end", "if", "while")
    keywords.map(expectKeywordNotId)
  }

  test("string") {
    expectSuccess("\"test str\"", STRING("test str"))
    // todo:true error info
    expectFailed("\"test str")
  }
  
  test("pending")(pending)

  test("") {
    expectSuccess("true false", List(TRUE, FALSE))
  }
}