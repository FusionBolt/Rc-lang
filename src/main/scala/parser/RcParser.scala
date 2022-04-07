package rclang
package parser

import ast.{ASTNode, Modules}
import lexer.Token

object RcParser extends ModuleParser {
  def apply(tokens: Seq[Token]): Either[RcParserError, ASTNode] = {
    doParser(tokens, program)
  }

  def program: Parser[ASTNode] = positioned {
    phrase(log(module)("module") ^^ (mods => Modules(List(mods))))
  }
}
