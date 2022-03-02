package rclang
package parser

import ast.AST
import lexer.Token

object RcParser extends ModuleParser {
  def apply(tokens: Seq[Token]): Either[RcParserError, AST] = {
    doParser(tokens, program)
  }

  def program: Parser[AST] = positioned {
    phrase(rep(module) ^^ (mods => AST.Modules(mods)))
  }
}
