package rclang
package parser

import ast.AST
import lexer.Token

object RcParser extends ModuleParser {
  def apply(tokens: Seq[Token]): Either[RcParserError, AST] = {
    doParser(tokens, program)
  }

  def program: Parser[AST] = positioned {
    phrase(log(module)("module") ^^ (mods => AST.Modules(List(mods))))
  }
}
