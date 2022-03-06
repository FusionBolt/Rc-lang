package rclang
package parser

import org.scalatest.funspec.AnyFunSpec

import ast.*
import ast.Expr.{Identifier, Number, Str, Binary}
import lexer.Token
import lexer.Token.*
import scala.language.postfixOps
import org.scalatest._
import org.scalactic.TimesOnInt.convertIntToRepeater

class ExprParserTest extends ExprParser with BaseParserTest {
  def apply(tokens: Seq[Token]): Either[RcParserError, Expr] = {
    doParser(tokens, expr)
  }

  def trueExpr = Expr.Bool(true)
  def falseExpr = Expr.Bool(false)

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

  def noEmptyEval[T](l: List[T], f: List[T] => List[T], els: List[T] = List()) = if l.isEmpty then els else f(l)
  def makeElsif(lists: List[(Token, Token)]): List[Token] = lists.map((x, y) => ELSIF::x::EOL::y::EOL::List()).reduce(_.concat(_))
  def makeIf(cond: List[Token], thenTokens: List[Token], elsifTokens: List[Token] = List(), elseTokens:List[Token] = List()): List[Token] =
    IF::cond
      .concat(EOL::thenTokens)
      .concat(noEmptyEval(elsifTokens, EOL::_))
      .concat(noEmptyEval(elseTokens, EOL::ELSE::_))
  def makeIf(cond: Token, thenToken: Token, elsifTokens: List[Token], elseToken: Token): List[Token] = makeIf(List(cond), List(thenToken), elsifTokens, List(elseToken))
  def makeIf(cond: List[Token], thenToken: Token, elsifTokens: List[Token], elseToken: Token): List[Token] = makeIf(cond, List(thenToken), elsifTokens, List(elseToken))
  def makeIf(cond: Token, thenToken: Token, elsifTokens: List[Token]): List[Token] = makeIf(List(cond), List(thenToken), elsifTokens, List())
  def makeIf(cond: Token, thenToken: Token, elseToken: Token): List[Token] = makeIf(List(cond), List(thenToken), List(), List(elseToken))

  describe("if") {
    it("full succeed") {
      expectSuccess(
        makeIf(TRUE, NUMBER(1), makeElsif(List((FALSE, NUMBER(2)))), NUMBER(3)),
          Expr.If(trueExpr, Number(1), List(Elsif(falseExpr, Number(2))), Some(Number(3))))
    }

    it("full with multi EOL") {
      expectSuccess(
        makeIf(List(TRUE, EOL, EOL), NUMBER(1), makeElsif(List((FALSE, NUMBER(2)))), NUMBER(3)),
        Expr.If(trueExpr, Number(1), List(Elsif(falseExpr, Number(2))), Some(Number(3))))
    }

    it("no elsif") {
      expectSuccess(makeIf(TRUE, NUMBER(1), NUMBER(3)),
        Expr.If(trueExpr, Number(1), List(), Some(Number(3))))
    }

    it("no else") {
      expectSuccess(makeIf(TRUE, NUMBER(1), makeElsif(List((FALSE, NUMBER(2))))),
        Expr.If(trueExpr, Number(1), List(Elsif(falseExpr, Number(2))), None))
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

class BinaryTranslatorTest extends AnyFunSpec with BinaryTranslator {
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