package rclang
package parser

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.*
import lexer.Token
import lexer.Keyword.*
import lexer.Punctuation.*
import lexer.Literal.*
import lexer.Delimiter.*
import lexer.Ident.*
import ast.{ASTBuilder, Expr, FieldDef, Ident, Item, MethodDecl, Param, Params, Stmt, TyInfo}
import ast.ImplicitConversions.*
import ast.Expr.{Block, If}

import scala.util.parsing.input.Positional

trait BaseParserTest extends AnyFunSpec with RcBaseParser with Matchers with ASTBuilder {
  def withParentThese(tokens: List[Token]) = List(LEFT_PARENT_THESES) ::: tokens ::: List(RIGHT_PARENT_THESES)
  def mkASTCall(target: String, generic: String, args: List[Expr]) = Expr.Call(target, args, Some(Ident(generic)))
  def wrapWithAngleBrackets(s: Token) = List(OPERATOR("<"), s, OPERATOR(">"))
  def wrapWithAngleBrackets(s: List[Token]) = List(OPERATOR("<")):::s:::List(OPERATOR(">"))
  def parSround(tokens: List[Token]): List[Token] = LEFT_PARENT_THESES::tokens:::RIGHT_PARENT_THESES::List()
  def makeWhile(cond: Token, body: List[Token]): List[Token] = WHILE::parSround(List(cond)):::EOL::body:::EOL::END::EOL::List()
  def mkTKArgs(argsTokens: List[Token]): List[Token] = parSround(sepWithComma(argsTokens))
  def mkTKArgsList(argsTokens: List[List[Token]]): List[Token] = parSround(sepListWithComma(argsTokens))
  def mkTkMemField(name: String, field: String) = List(IDENTIFIER(name), DOT, IDENTIFIER(field))

  def mkAssStmt(name: String, expr: Token): List[Token] = List(IDENTIFIER(name), EQL, expr, EOL)
  def mkLocalStmt(name: String, expr: Token): List[Token] = List(VAR, IDENTIFIER(name), EQL, expr, EOL)

  def makeCall(name: String, args: List[Token]): List[Token] =
    IDENTIFIER(name)::LEFT_PARENT_THESES::
      noEmptyEval(args, _ =>
        args.zip(List.fill(args.length - 1)(COMMA).appended(RIGHT_PARENT_THESES))
          .flatten{ case (a, b) => List(a, b) },
        List(RIGHT_PARENT_THESES))

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

  def mkTkMemCall(name: String, field: String, args: List[Token] = List()): List[Token] =
    List(IDENTIFIER(name), DOT, IDENTIFIER(field),
      LEFT_PARENT_THESES):::args:::RIGHT_PARENT_THESES::Nil


  def makeTokenMethod(name: String, stmts: List[Token] = List()): List[Token] = {
    List(DEF, IDENTIFIER(name), LEFT_PARENT_THESES, RIGHT_PARENT_THESES, EOL):::(stmts).appended(END).appended(EOL)
  }

  def mkGenericToken(generic: Option[String]) = {
    generic.map(s => wrapWithAngleBrackets(UPPER_IDENTIFIER(s))).getOrElse(List())
  }

  def mkEmptyTokenMethod(name: String, params: List[Token] = List(), generic: Option[String] = None): List[Token] = {
    List(DEF, IDENTIFIER(name)):::mkGenericToken(generic):::List(LEFT_PARENT_THESES):::params:::List(RIGHT_PARENT_THESES):::List(EOL, END, EOL)
  }

  def sepWithComma(tokens: List[Token]): List[Token] = {
    sepListWithComma(tokens.map(List(_)))
  }

  def sepListWithComma(tokens: List[List[Token]]): List[Token] = {
    tokens.flatMap(_.appended(COMMA)).init
  }

  def makeLocal(name: String, value: Token) = {
    List(VAR, IDENTIFIER(name), EQL, value, EOL)
  }

  def mkTokenField(name: String, ty: String) = List(VAR, IDENTIFIER(name), COLON, UPPER_IDENTIFIER(ty), EOL)
  def mkTokenClass(name: String, tokens: List[Token] = List(), generic: Option[String] = None) = List(CLASS, UPPER_IDENTIFIER(name)):::mkGenericToken(generic):::List(EOL):::tokens:::END::EOL::Nil
  def mkTokenClass(name: String, parent: String) = List(CLASS, UPPER_IDENTIFIER(name), OPERATOR("<"), UPPER_IDENTIFIER(parent), EOL):::END::EOL::Nil

}
