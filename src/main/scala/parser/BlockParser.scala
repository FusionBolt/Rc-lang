package rclang
package parser

import rclang.ast.{Block, RcAST, RcExpr, Statement, Type}
import rclang.lexer.RcToken
import rclang.lexer.RcToken._
import parser.RcParser
import scala.util.parsing.input.{NoPosition, Position}

import scala.util.parsing.combinator.Parsers

trait BlockParser extends RcParser with ExprParser {
  def block: Parser[Block] = positioned {
    repsep(statement, EOL) ^^ (stmts => Block(stmts))
  }

  def local: Parser[Statement] = positioned {
    VAR ~> identifier <~ EQL ^^ {
      case IDENTIFIER(id) => Statement.Local(id, Type.Nil)
    }
  }

  def statement: Parser[Statement] = positioned {
    expr ^^ (exp => Statement.Expr(exp)) |
      local
  }
}