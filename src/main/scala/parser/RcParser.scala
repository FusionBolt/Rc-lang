package rclang
package parser

import ast.{ASTNode, Modules, RcModule}
import lexer.Token

object RcParser extends ModuleParser {
  def apply(tokens: Seq[Token]): Either[RcParserError, RcModule] = {
    doParser(tokens, program)
  }

  def program: Parser[RcModule] = positioned {
    phrase(log(module)("module"))
  }
}
