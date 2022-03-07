package rclang
package parser

import org.scalatest.funspec.AnyFunSpec

import ast.*
import ast.Expr.{Identifier, Number, Str, Binary}
import lexer.Token
import lexer.Token.*
import scala.language.postfixOps
import parser.RcBaseParser
import org.scalatest._
import org.scalactic.TimesOnInt.convertIntToRepeater

class ExprParserTest extends ExprParser with BaseParserTest {
  def apply(tokens: Seq[Token]): Either[RcParserError, Expr] = {
    doParser(tokens, expr)
  }

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

  def makeCall(name: String, args: List[Token]): List[Token] =
    IDENTIFIER(name)::LEFT_PARENT_THESES::
      noEmptyEval(args, _ =>
        args.zip(List.fill(args.length - 1)(COMMA).appended(RIGHT_PARENT_THESES))
          .flatten{ case (a, b) => List(a, b) },
        List(RIGHT_PARENT_THESES))

  describe("call") {
    it("empty args") {
      expectSuccess(makeCall("foo", List()), Expr.Call("foo", List()))
    }

    it("multi args") {
      expectSuccess(makeCall("foo", List(NUMBER(1), NUMBER(2))), Expr.Call("foo", List(Number(1), Number(2))))
    }
  }

  describe("binary") {
    it("single add") {
      expectSuccess(List(NUMBER(1), OPERATOR("+"), NUMBER(2)), Expr.Binary("+", Number(1), Number(2)))
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
      assert(replaceBinaryOp(oneBn, 1) == List(Binary("+", Number(1), Number(2))))
    }
  }

  describe("compose") {
    it("succeed") {
      assert(termsToBinary(addAndLT) == Binary("+", Number(1), Binary("<", Number(2), Number(3))))
    }
  }
}