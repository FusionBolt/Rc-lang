package rclang
package parser

import org.scalatest.funspec.AnyFunSpec
import rclang.ast.{Block, RcExpr}
import rclang.lexer.Token

class BlockParserTest extends AnyFunSpec with BlockParser {
  def apply(tokens: Seq[Token]): Either[RcParserError, Block] = {
    val reader = new RcTokenReader(tokens)
    block(reader) match {
      case NoSuccess(msg, next) => Left(RcParserError(Location(next.pos.line, next.pos.column), msg))
      case Success(result, next) => Right(result)
    }
  }
}