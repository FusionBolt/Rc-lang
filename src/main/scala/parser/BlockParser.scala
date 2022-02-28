package rclang
package parser

import ast.{Block, AST, RcExpr, Stmt, Type}
import lexer.Token
import lexer.Token._
import parser.RcBaseParser
import scala.util.parsing.input.{NoPosition, Position}

import scala.util.parsing.combinator.Parsers

trait BlockParser extends RcBaseParser with ExprParser {
  def block: Parser[Block] = positioned {
    repsep(statement, EOL) ^^ (stmts => Block(stmts))
  }

  def local: Parser[Stmt] = positioned {
    VAR ~> identifier <~ EQL ^^ {
      case IDENTIFIER(id) => Stmt.Local(id, Type.Nil)
    }
  }

  def statement: Parser[Stmt] = positioned {
    expr ^^ Stmt.Expr |
      local
  }
}