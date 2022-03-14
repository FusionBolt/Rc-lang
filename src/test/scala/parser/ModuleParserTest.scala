package rclang
package parser

import org.scalatest.funspec.AnyFunSpec
import lexer.Token
import lexer.Token.*

import ast.{Params, AST, Expr, Item}
import ast.Expr.*
import ast.*

class ModuleParserTest extends BaseParserTest with ModuleParser {
  def apply(tokens: Seq[Token]): Either[RcParserError, Item] = {
    doParser(tokens, item)
  }

  def expectSuccess(token: Seq[Token], expect: Item): Unit = {
    apply(token) match {
      case Left(value) => assert(false, value.msg)
      case Right(value) => assert(value == expect)
    }
  }

  def expectFailed(token: Seq[Token]): Unit = {
    apply(token) match {
    // todo:check failed pos
      case Left(value) => assert(true)
      case Right(value) => assert(false, s"expect failed, value: $value")
    }
  }

  describe("fun") {
    it("empty") {
      expectSuccess(makeTokenMethod("foo"),
        makeASTMethod("foo"))
    }

    it("with one line") {
      expectSuccess(
        makeTokenMethod("foo", makeLocal("a", NUMBER(1))),
        makeASTMethod("foo", block = List(Stmt.Local("a", Type.Nil, Number(1)))))
    }

    it("with multi line") {
      expectSuccess(
        makeTokenMethod("foo",
          makeLocal("a", NUMBER(1))
            .concat(makeLocal("a", NUMBER(1)))),
        makeASTMethod("foo",
          block = List(
            makeLocal("a", Number(1)),
            makeLocal("a", Number(1)))))
    }

    it("multi line with multi eol") {
      expectSuccess(
        makeTokenMethod("foo",
          makeLocal("a", NUMBER(1))
            :::(EOL::EOL::List())
            :::(makeLocal("a", NUMBER(1)))),
        makeASTMethod("foo",
          block = List(
            makeLocal("a", Number(1)),
            makeLocal("a", Number(1)))))
    }
  }

  describe("class") {
    it("empty class") {
      expectSuccess(makeTokenClass("Foo"), makeAstClass("Foo"))
    }

    it("class with method") {
      expectSuccess(makeTokenClass("Foo", makeTokenMethod("a")), makeAstClass("Foo", makeASTMethod("a")))
    }

    it("must uppercase") {
      expectFailed(List(CLASS, IDENTIFIER("foo"), EOL, END, EOL))
    }

    it("not supported oneline class") {
      expectFailed(List(CLASS, IDENTIFIER("Foo"), END))
    }
  }
}