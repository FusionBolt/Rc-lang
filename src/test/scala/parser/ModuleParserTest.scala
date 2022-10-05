package rclang
package parser

import org.scalatest.funspec.AnyFunSpec
import lexer.Token
import lexer.Keyword.*
import lexer.Punctuation.*
import lexer.Literal.*
import lexer.Delimiter.*
import lexer.Ident.*

import ast.{Params, Expr, Item}
import ast.Expr.*
import ast.*
import ast.ImplicitConversions.*

class ModuleParserTest extends BaseParserTest with ModuleParser {
  def apply(tokens: Seq[Token]): Either[RcParserError, Item] = {
    doParser(tokens, item)
  }

  def expectSuccess(token: Seq[Token], expect: Item): Unit = {
    apply(token) match {
      case Left(value) => assert(false, value.msg)
      case Right(value) => {
        assert(value == expect)
      }
    }
  }

  def expectFailed(token: Seq[Token]): Unit = {
    apply(token) match {
      case Left(value) => assert(true)
      case Right(value) => assert(false, s"expect failed, value: $value")
    }
  }

  describe("fun") {
    describe("params") {
      it("not spec type") {
        expectSuccess(
          mkEmptyTokenMethod("f",
            sepWithComma(List(IDENTIFIER("a"), IDENTIFIER("b")))),
          makeASTMethod("f",
            List(Param("a", TyInfo.Infer),
              Param("b", TyInfo.Infer))))
      }

      it("spec type") {
        expectSuccess(
          mkEmptyTokenMethod("f",
            sepListWithComma(List(
              List(IDENTIFIER("a"), COLON, UPPER_IDENTIFIER("Int")),
              List(IDENTIFIER("a"), COLON, UPPER_IDENTIFIER("Int"))))),
          makeASTMethod("f",
            List(
              Param("a", TyInfo.Spec("Int")),
              Param("a", TyInfo.Spec("Int"))
            )))
      }
    }

    describe("body") {
      it("empty") {
        expectSuccess(makeTokenMethod("foo"),
          makeASTMethod("foo"))
      }

      it("with one line") {
        expectSuccess(
          makeTokenMethod("foo", makeLocal("a", NUMBER(1))),
          makeASTMethod("foo", block = List(Stmt.Local("a", TyInfo.Infer, Number(1)))))
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
              ::: (EOL :: EOL :: List())
              ::: (makeLocal("a", NUMBER(1)))),
          makeASTMethod("foo",
            block = List(
              makeLocal("a", Number(1)),
              makeLocal("a", Number(1)))))
      }
    }
  }

  describe("class") {
    it("empty class") {
      expectSuccess(mkTokenClass("Foo"), mkASTClass("Foo"))
    }

    it("class with var") {
      expectSuccess(mkTokenClass("Foo", mkTokenField("a", "Int")), mkASTClass("Foo", mkASTField("a", "Int")))
    }

    it("class with method") {
      expectSuccess(mkTokenClass("Foo", makeTokenMethod("a")), mkASTClass("Foo", makeASTMethod("a")))
    }

    describe("class with method and var") {
      it("normal succeed") {
        expectSuccess(
          mkTokenClass("Foo", makeTokenMethod("f1") ::: mkTokenField("a", "Int")),
          mkASTClass("Foo", mkASTField("a", "Int"), makeASTMethod("f1")))
      }

      it("include eol") {
        expectSuccess(
          mkTokenClass("Foo", makeTokenMethod("f1") ::: EOL :: mkTokenField("a", "Int")):::EOL::Nil,
          mkASTClass("Foo", mkASTField("a", "Int"), makeASTMethod("f1")))
      }
    }

    it("must uppercase") {
      expectFailed(List(CLASS, IDENTIFIER("foo"), EOL, END, EOL))
    }

    it("not supported oneline class") {
      expectFailed(List(CLASS, IDENTIFIER("Foo"), END))
    }

    it("inherit") {
      expectSuccess(mkTokenClass("Foo", "Parent"), mkASTClass("Foo", "Parent"))
    }

    describe("multiModuleItem") {
      def apply(tokens: Seq[Token]): Either[RcParserError, RcModule] = {
        doParser(tokens, module)
      }
      def expectSuccess(token: Seq[Token], expect: RcModule): Unit = {
        apply(token) match {
          case Left(value) => assert(false, value.msg)
          case Right(value) => assert(value == expect)
        }
      }

      // 1. parse ok
      // 2. filter None
      it("splitWithEol") {
        expectSuccess(
          mkTokenClass("Foo"):::EOL::mkEmptyTokenMethod("f"),
          RcModule(List(mkASTClass("Foo"), makeASTMethod("f"))))
      }
    }
  }
}