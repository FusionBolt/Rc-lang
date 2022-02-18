package rclang
package lexer

import org.scalatest.funsuite.AnyFunSuite
import rclang.lexer.RcLexer

class LexerTest extends AnyFunSuite {
  def expectSuccess(str: String) = {
    RcLexer(str) match {
      case Left(value) => assert(false)
      // todo:expect value is eq
      // todo:print error info
      case Right(value) =>
    }
  }

  test("number") {
    expectSuccess("123")
  }

  test("bool") {
    expectSuccess("true")
    expectSuccess("false")
  }

  test("identifier") {
    // todo:expect failed
    // todo:test keyword is not a id
    expectSuccess("foo")
    expectSuccess("foo1")
  }

  test("string") {
    expectSuccess("\"test str\"")
  }
  
  test("pending")(pending)
}