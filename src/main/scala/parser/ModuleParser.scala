package rclang
package parser

import ast.*
import lexer.*
import lexer.Token.*
import parser.RcParser

import scala.util.parsing.combinator.Parsers
import scala.util.parsing.input.{NoPosition, Position, Reader}

trait ModuleParser extends RcBaseParser with ExprParser with StmtParser {
  def define: Parser[Item] = method

  def args: Parser[Params] = positioned {
    LEFT_PARENT_THESES ~ repsep(identifier, COMMA) ~ RIGHT_PARENT_THESES ^^ {
      case _ ~ params ~ _ => Params(params.map(x => Param(x.str)))
    }
  }

  def method: Parser[Item] = positioned {
    DEF ~ identifier ~ args ~ block ~ END ^^ {
      case _ ~ IDENTIFIER(id) ~ args ~ block ~ _ => Item.Method(MethodDecl(id, args, Type.Nil), block)
    }
  }

  def block: Parser[Block] = positioned {
    repsep(statement, EOL) ^^ (stmts => Block(stmts))
  }

  def item: Parser[Item] = positioned {
    method
  }

  def module: Parser[RcModule] = positioned {
    rep(item) ^^ RcModule
  }
}