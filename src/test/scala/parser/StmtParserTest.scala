package rclang
package parser

import org.scalatest.funspec.AnyFunSpec
import rclang.ast.*
import rclang.ast.Expr.Number
import rclang.lexer.Token.*
import rclang.lexer.Token

class StmtParserTest extends BaseParserTest with StmtParser {
  def apply(tokens: Seq[Token]): Either[RcParserError, Stmt] = {
    doParser(tokens, statement)
  }

  def expectSuccess(token: Seq[Token], expect: Stmt): Unit = {
    apply(token) match {
      case Left(value) => assert(false, value.msg)
      case Right(value) => assert(value == expect)
    }
  }

  describe("local") {
    it("succeed") {
      expectSuccess(List(VAR, IDENTIFIER("a"), EQL, NUMBER(1), EOL), Stmt.Local("a", Type.Nil, Expr.Number(1)))
    }
  }

  describe("expr") {
    it("succeed") {
      expectSuccess(List(NUMBER(1), EOL), Stmt.Expr(Expr.Number(1)))
    }
  }

  describe("return") {
    it("succeed") {
      expectSuccess(List(RETURN, NUMBER(1), EOL), Stmt.Return(Expr.Number(1)))
    }
  }

  describe("assign") {
    it("succeed") {
      expectSuccess(List(IDENTIFIER("a"), EQL, NUMBER(1), EOL), Stmt.Assign("a", Expr.Number(1)))
    }
  }


  def trueExpr = Expr.Bool(true)
  def falseExpr = Expr.Bool(false)
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
        Stmt.Expr(Expr.If(trueExpr, Number(1), List(Elsif(falseExpr, Number(2))), Some(Number(3)))))
    }

    it("full with multi EOL") {
      expectSuccess(
        makeIf(List(TRUE, EOL, EOL), NUMBER(1), makeElsif(List((FALSE, NUMBER(2)))), NUMBER(3)),
        Stmt.Expr(Expr.If(trueExpr, Number(1), List(Elsif(falseExpr, Number(2))), Some(Number(3)))))
    }

    it("no elsif") {
      expectSuccess(makeIf(TRUE, NUMBER(1), NUMBER(3)),
        Stmt.Expr(Expr.If(trueExpr, Number(1), List(), Some(Number(3)))))
    }

    it("no else") {
      expectSuccess(makeIf(TRUE, NUMBER(1), makeElsif(List((FALSE, NUMBER(2))))),
        Stmt.Expr(Expr.If(trueExpr, Number(1), List(Elsif(falseExpr, Number(2))), None)))
    }
  }
}