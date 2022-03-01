package rclang
package parser

import ast.*
import lexer.Token
import lexer.Token._
import parser.RcBaseParser
import scala.util.parsing.input.{NoPosition, Position}

import scala.util.parsing.combinator.Parsers

trait StmtParser extends RcBaseParser with ExprParser {
  def statement: Parser[Stmt] = positioned {
    expr ^^ Stmt.Expr
      | local
      | ret
  }

  def local: Parser[Stmt] = positioned {
    (VAR ~> identifier) ~ (EQL ~> expr) ^^ {
      case IDENTIFIER(id) ~ expr => Stmt.Local(id, Type.Nil, expr)
    }
  }

  def ret: Parser[Stmt.Return] = positioned {
    RETURN ~> expr ^^ Stmt.Return
  }
}