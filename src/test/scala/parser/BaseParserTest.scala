package rclang
package parser

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.*
import lexer.Token
import lexer.Token.*

import rclang.ast.{Expr, Stmt}
import rclang.ast.Expr.{Block, If}

trait BaseParserTest extends AnyFunSpec with RcBaseParser with Matchers {
  def parSround(tokens: List[Token]): List[Token] = LEFT_PARENT_THESES::tokens:::RIGHT_PARENT_THESES::List()
  def makeWhile(cond: Token, body: List[Token]): List[Token] = WHILE::parSround(List(cond)):::EOL::body:::EOL::END::EOL::List()

  def mkAssStmt(name: String, expr: Token): List[Token] = List(IDENTIFIER(name), EQL, expr, EOL)
  def mkLocalStmt(name: String, expr: Token): List[Token] = List(VAR, IDENTIFIER(name), EQL, expr, EOL)

  def trueExpr = Expr.Bool(true)
  def falseExpr = Expr.Bool(false)
  def makeElsif(lists: List[(Token, Token)]): List[Token] = lists.map((x, y) => ELSIF::x::EOL::y::EOL::List()).reduce(_:::_)
  def makeIf(cond: List[Token], thenTokens: List[Token], elsifTokens: List[Token] = List(), elseTokens:List[Token] = List()): List[Token] =
    IF::cond
      .concat(EOL::thenTokens)
      .concat(noEmptyEval(elsifTokens, EOL::_))
      .concat(noEmptyEval(elseTokens, EOL::ELSE::EOL::_))
      .appended(EOL)
      .appended(END)
  def makeIf(cond: Token, thenToken: Token, elsifTokens: List[Token], elseToken: Token): List[Token] = makeIf(List(cond), List(thenToken), elsifTokens, List(elseToken))
  def makeIf(cond: List[Token], thenToken: Token, elsifTokens: List[Token], elseToken: Token): List[Token] = makeIf(cond, List(thenToken), elsifTokens, List(elseToken))
  def makeIf(cond: Token, thenToken: Token, elsifTokens: List[Token]): List[Token] = makeIf(List(cond), List(thenToken), elsifTokens, List())
  def makeIf(cond: Token, thenToken: Token, elseToken: Token): List[Token] = makeIf(List(cond), List(thenToken), List(), List(elseToken))

  def makeExprBlock(cond: Expr): Block = Block(List(Stmt.Expr(cond)))
  def makeStmtBlock(cond: Stmt): Block = Block(List(cond))
  def makeIf(cond: Expr, thenExpr: Expr, elseExpr: Expr) = If(cond, makeExprBlock(thenExpr), Some(elseExpr))
  def makeLastIf(cond: Expr, thenExpr: Expr, elseExpr: Expr) = If(cond, makeExprBlock(thenExpr), Some(makeExprBlock(elseExpr)))
  def makeIf(cond: Expr, thenExpr: Expr, elseExpr: Option[Expr]) = If(cond, makeExprBlock(thenExpr), elseExpr)

}
