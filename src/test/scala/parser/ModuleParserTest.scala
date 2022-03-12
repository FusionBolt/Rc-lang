package rclang
package parser

import org.scalatest.funspec.AnyFunSpec
import lexer.Token
import lexer.Token.*

import ast.{Params, AST, Expr, Item}
import ast.Expr.*
import ast.*

class ModuleParserTest extends AnyFunSpec with ModuleParser {
  def apply(tokens: Seq[Token]): Either[RcParserError, Item] = {
    doParser(tokens, item)
  }

  def expectSuccess(token: Seq[Token], expect: Item): Unit = {
    apply(token) match {
      case Left(value) => assert(false, value.msg)
      case Right(value) => assert(value == expect)
    }
  }

  def makeTokenMethod(name: String, stmts: List[Token] = List()): Seq[Token] = {
    List(DEF, IDENTIFIER("foo"), LEFT_PARENT_THESES, RIGHT_PARENT_THESES, EOL):::(stmts).appended(END).appended(EOL)
  }

  def makeLocal(name: String, value: Token) = {
    List(VAR, IDENTIFIER(name), EQL, value, EOL)
  }
  def makeLocal(name: String, value: Expr) = Stmt.Local(name, Type.Nil, value)

  def makeASTMethod(name: String,
                    params: List[Param] = List(),
                    ret_type:Type = Type.Nil,
                    block: List[Stmt] = List()): Item = {
    Item.Method(MethodDecl(name, Params(params), ret_type), Block(block))
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
}