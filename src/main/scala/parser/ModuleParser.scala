package rclang
package parser

import rclang.ast.*
import rclang.lexer.*
import rclang.lexer.RcToken.*
import parser.RcParser

import scala.util.parsing.combinator.Parsers
import scala.util.parsing.input.{NoPosition, Position, Reader}

object ModuleParser extends RcParser with ExprParser with BlockParser {
  def apply(tokens: Seq[RcToken]): Either[RcParserError, RcAST] = {
    val reader = new RcTokenReader(tokens)
    program(reader) match {
      case NoSuccess(msg, next) => Left(RcParserError(Location(next.pos.line, next.pos.column), msg))
      case Success(result, next) => Right(result)
    }
  }

  def program: Parser[RcAST] = positioned {
    phrase(define ^^ (defs => RcAST.Define(defs)) | expr ^^ (exprs => RcAST.Expr(exprs)))
  }

  def define: Parser[RcItem] = method

  def args: Parser[Params] = positioned {
    LEFT_PARENT_THESES ~ repsep(identifier, COMMA) ~ RIGHT_PARENT_THESES ^^ {
      case _ ~ params ~ _ => Params(params.map(x => Param(x.str)))
    }
  }

  def method: Parser[RcItem] = positioned {
    DEF ~ identifier ~ args ~ block ~ END ^^ {
      case _ ~ IDENTIFIER(id) ~ args ~ block ~ _ => RcItem.Method(MethodDecl(id, args, Type.Nil), block)
    }
  }
}