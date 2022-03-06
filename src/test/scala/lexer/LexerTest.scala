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
      case Right(value) =>assert(value == tokens, if value.isEmpty then "" else value.last.pos)
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

  describe("operator") {
    it("succeed") {
      def expectOp(op: Char) = expectSuccess(op.toString, OPERATOR(op.toString))

      "+-*/%^~!<>".foreach(expectOp)
    }
  }

  describe("space split") {
    // a is splitWithSpace, b is canNoSpace
    it("ABA") {
      expectSuccess("id id", List(IDENTIFIER("id"), IDENTIFIER("id")))
    }

    it("BAB") {
      expectSuccess(" id ", List(IDENTIFIER("id")))
    }

    it("ABABB space and eol") {
      expectSuccess("def f \n", List(DEF, IDENTIFIER("f"), EOL))
    }

    it("BABA") {
      expectSuccess(" def f", List(DEF, IDENTIFIER("f")))
    }

    it("only space") {
      expectSuccess(" ", List())
    }

    it("local") {
      val v = List(IDENTIFIER("a"), EQL, NUMBER(1))
      expectSuccess("a = 1", v)
      expectSuccess("a = 1 ", v)
      expectSuccess("a =1", v)
      expectSuccess("a=1", v)
    }
  }

  describe ("eol") {
    it("basic succeed") {
      expectSuccess("id \n id", List(IDENTIFIER("id"), EOL, IDENTIFIER("id")))
    }

    it("succeed") {
      expectSuccess("def main \n end", List(DEF, IDENTIFIER("main"), EOL, END))
    }
  }

  describe("fun") {
    it("empty") {
      val src = """def main end"""
      expectSuccess(src, List(DEF, IDENTIFIER("main"), END))
    }

    it("with local") {
      val src = """def main
  var a = 1
end"""
      expectSuccess(src, List(DEF, IDENTIFIER("main"), EOL, VAR, IDENTIFIER("a"), EQL, NUMBER(1), EOL, END))
    }

    it("with if") {
      val src = """def main
  if a < 3
    a = 1
  else
    a = 2
end"""
      expectSuccess(src, List(DEF, IDENTIFIER("main"), EOL,
        IF, IDENTIFIER("a"), OPERATOR("<"), NUMBER(3), EOL,
        IDENTIFIER("a"), EQL, NUMBER(1), EOL,
        ELSE, EOL,
        IDENTIFIER("a"), EQL, NUMBER(2), EOL,
        END))
    }

    it("with call") {
      val src = """def main
  put_i(a)
end"""
      expectSuccess(src, List(DEF, IDENTIFIER("main"), EOL,
        IDENTIFIER("put_i"), LEFT_PARENT_THESES, IDENTIFIER("a"), RIGHT_PARENT_THESES, EOL,
        END))
    }
  }
}
