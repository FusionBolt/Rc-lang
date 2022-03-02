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

  def makeTokenMethod(name: String): Seq[Token] = {
    List(DEF, IDENTIFIER("foo"), LEFT_PARENT_THESES, RIGHT_PARENT_THESES, END)
  }

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
  }
}