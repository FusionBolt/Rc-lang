package rclang
package parser

import org.scalatest.funspec.AnyFunSpec
import lexer.Keyword.*
import lexer.Punctuation.*
import lexer.Literal.*
import lexer.Delimiter.*
import lexer.Ident.*
import ast.*
import ast.Expr.Number
import lexer.Token

class StmtParserTest extends BaseParserTest with ExprParser {
  def apply(tokens: Seq[Token]): Either[RcParserError, (Stmt, Input)] = {
    doParserImpl(tokens, statement)
  }

  describe("expr") {
    it("succeed") {
      expectSuccess(List(NUMBER(1), EOL), Stmt.Expr(Expr.Number(1)))
    }
  }

  def expectSuccess(token: Seq[Token], expect: Stmt): Unit = {
    apply(token) match {
      case Left(value) => assert(false, value.msg)
      case Right((ast, reader)) => assert(ast == expect); assert(reader.atEnd, reader)
    }
  }

  describe("local") {
    it("succeed") {
      expectSuccess(mkLocalStmt("a", NUMBER(1)), Stmt.Local("a", Type.Nil, Expr.Number(1)))
    }
  }

  describe("assign") {
    it("succeed") {
      expectSuccess(mkAssStmt("a", NUMBER(1)), Stmt.Assign("a", Expr.Number(1)))
    }
  }

  describe("while") {
    it("succeed") {
      expectSuccess(
        makeWhile(TRUE, mkAssStmt("a", NUMBER(1))),
        Stmt.While(trueExpr, makeStmtBlock(Stmt.Assign("a", Expr.Number(1)))))
    }
  }
}