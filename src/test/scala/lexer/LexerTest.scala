package rclang
package lexer

import org.scalatest.funsuite.AnyFunSuite
import lexer.Lexer
import lexer.Token.*

import org.scalatest.funspec.AnyFunSpec

class LexerTest extends AnyFunSuite {
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
    val keywords = List("true", "false", "def", "end", "if", "elsif", "else", "while", "class", "super")
    keywords.map(expectKeywordNotId)
  }

  test("string") {
    expectSuccess("\"test str\"", STRING("test str"))
    // todo:true error info
    expectFailed("\"test str")
  }

  test ("eol") {
    expectSuccess("id \n id", List(IDENTIFIER("id"), EOL, IDENTIFIER("id")))
  }

  test("") {
    expectSuccess("true false", List(TRUE, FALSE))
  }

  test("operator") {
    def expectOp(op: Char) = expectSuccess(op.toString, OPERATOR(op.toString))
    "+-*/%^~!".foreach(expectOp)
  }
}