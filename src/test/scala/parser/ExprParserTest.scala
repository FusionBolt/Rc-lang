package rclang
package parser

import org.scalatest.funspec.AnyFunSpec

import ast.*
import ast.Expr.*
import ast.BinaryOp.*
import ast.ImplicitConversions.*
import lexer.Token
import lexer.Keyword.*
import lexer.Punctuation.*
import lexer.Literal.*
import lexer.Delimiter.*
import lexer.Ident.*
import scala.language.postfixOps
import parser.RcBaseParser
import org.scalatest._
import org.scalactic.TimesOnInt.convertIntToRepeater

class ExprParserTest extends ExprParser with BaseParserTest {
  def apply(tokens: Seq[Token]): Either[RcParserError, (Expr, Input)] = {
    doParserImpl(tokens, expr)
  }

  def expectSuccess(token: Token, expect: Expr): Unit = {
    expectSuccess(List(token), expect)
  }

  def expectSuccess(tokens: Seq[Token], expect: Expr): Unit = {
    apply(tokens) match {
      case Left(value) => assert(false, value.msg)
      case Right((ast, reader)) => assert(ast == expect);assert(reader.atEnd, reader)
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

  describe("const") {
    it("succeed") {
      expectSuccess(UPPER_IDENTIFIER("Foo"), Expr.Symbol("Foo"))
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

  describe("call") {
    it("empty args") {
      expectSuccess(makeCall("foo", List()), Call("foo", List()))
    }

    it("multi args") {
      expectSuccess(makeCall("foo", List(NUMBER(1), NUMBER(2))), Call("foo", List(Number(1), Number(2))))
    }
  }

  describe("memField") {
    it("normalField") {
     expectSuccess(mkTkMemField("homura", "shield"), mkASTMemField("homura", "shield"))
    }

    it("selfField") {
      expectSuccess(List(AT, IDENTIFIER("homura")), Expr.Field(Self, "homura"))
    }
  }

  describe("memCall") {
    it("succeed") {
      expectSuccess(mkTkMemCall("homura", "shot"), mkASTMemCall("homura", "shot"))
    }

    it("new") {
      expectSuccess(
        List(UPPER_IDENTIFIER("Foo"), DOT, IDENTIFIER("new"), LEFT_PARENT_THESES, RIGHT_PARENT_THESES),
        MethodCall(Symbol("Foo"), "new", List()))
    }
  }

  describe("arrayIndex") {
    it("normal number") {
      expectSuccess(
        List(IDENTIFIER("a"), LEFT_SQUARE, NUMBER(1), RIGHT_SQUARE),
        Index(Identifier("a"), Number(1)))
    }

    // todo:binary op is enum
    it("index is termExpr") {
        expectSuccess(
          List(IDENTIFIER("a"), LEFT_SQUARE, NUMBER(1), OPERATOR("+"), NUMBER(2), RIGHT_SQUARE),
          Index(Identifier("a"), Binary(Add, Number(1), Number(2))))
    }
  }

  describe("binary") {
    it("single add") {
      expectSuccess(List(NUMBER(1), OPERATOR("+"), NUMBER(2)), Expr.Binary(Add, Number(1), Number(2)))
    }
  }

  describe("return") {
    it("succeed") {
      expectSuccess(List(RETURN, NUMBER(1)), Expr.Return(Expr.Number(1)))
    }
  }
  
  // todo:if without eol is error
  describe("if") {
    it("full succeed") {
      expectSuccess(
        makeIf(TRUE, NUMBER(1), makeElsif(List((FALSE, NUMBER(2)))), NUMBER(3)),
        makeIf(trueExpr, Number(1), makeLastIf(falseExpr, Number(2), Number(3))))
    }

    it("full with multi EOL") {
      expectSuccess(
        makeIf(List(TRUE, EOL, EOL), NUMBER(1), makeElsif(List((FALSE, NUMBER(2)))), NUMBER(3)),
        makeIf(trueExpr, Number(1), makeLastIf(falseExpr, Number(2), Number(3))))
    }

    it("no elsif") {
      expectSuccess(makeIf(TRUE, NUMBER(1), NUMBER(3)),
        makeLastIf(trueExpr, Number(1), Number(3)))
    }

    it("no else") {
      expectSuccess(makeIf(TRUE, NUMBER(1), makeElsif(List((FALSE, NUMBER(2))))),
        makeIf(trueExpr, Number(1), makeIf(falseExpr, Number(2), None)))
    }
  }
}

class BinaryTranslatorTest extends BaseParserTest with BinaryTranslator {
  // todo:impl convert
  def makeBinary(a: Int, op: String, b: Int) = List(Number(a), OPERATOR(op), Number(b))
  def makeMultiBinary(a: Int, op1: String, b: Int, op2: String, c:Int) =
    List(Number(a), OPERATOR(op1), Number(b), OPERATOR(op2), Number(c))

  def oneBn = makeBinary(1, "+", 2)
  def twoAdd = makeMultiBinary(1, "+", 2, "+", 3)
  def addAndLT = makeMultiBinary(1, "+", 2, "<", 3)

  describe("findMaxInfixIndex") {
    it("only one op") {
      assert(findMaxInfixIndex(oneBn) == 1)
    }
    it("multi same infix op") {
      assert(findMaxInfixIndex(twoAdd) == 1)
    }
    it("multi different op") {
      assert(findMaxInfixIndex(addAndLT) == 3)
    }
  }

  describe("replaceBinaryOp") {
    it("succeed") {
      assert(replaceBinaryOp(oneBn, 1) == List(Binary(Add, Number(1), Number(2))))
    }
  }

  describe("compose") {
    it("succeed") {
      assert(termsToBinary(addAndLT) == Binary(Add, Number(1), Binary(LT, Number(2), Number(3))))
    }
  }
}