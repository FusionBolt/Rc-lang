package rclang
package parser

import ast.AST
import lexer.Token

object RcParser extends ModuleParser {
  def apply(tokens: Seq[Token]): Either[RcParserError, AST] = {
    val reader = new RcTokenReader(tokens)
    program(reader) match {
      case NoSuccess(msg, next) => Left(RcParserError(Location(next.pos.line, next.pos.column), msg))
      case Success(result, next) => Right(result)
    }
  }

  def program: Parser[AST] = positioned {
    phrase(rep(module) ^^ (mods => AST.Modules(mods)))
  }
}
