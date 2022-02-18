package rclang
package lexer

import rclang.lexer.RcLexer
import rclang.lexer.RcToken._
import rclang.RcLexerError
import rclang.Location
import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers

object RcLexer extends RegexParsers {
  override def skipWhitespace = true
  override val whiteSpace: Regex = "[ \t\r\f]+".r

  def apply(code: String): Either[RcLexerError, List[RcToken]] = {
    parse(tokens, code) match {
      case NoSuccess(msg, next) => Left(RcLexerError(Location(next.pos.line, next.pos.column), msg))
      case Success(result, next) => Right(result)
    }
  }

  def tokens: Parser[List[RcToken]] = {
    phrase(rep1(stringLiteral | trueLiteral | falseLiteral | number |identifier))
  }

  def identifier: Parser[IDENTIFIER] = positioned {
    "[a-zA-Z_][a-zA-Z0-9_]*".r ^^ { str => IDENTIFIER(str) }
  }

  def stringLiteral: Parser[STRING] = positioned {
    """"[^"]*"""".r ^^ { str =>
      val content = str.substring(1, str.length - 1)
      STRING(content)
    }
  }

  def number = positioned {
    """(0|[1-9]\d*)""".r ^^ { i => NUMBER(i.toInt) }
  }

  def trueLiteral = positioned { "true" ^^ (_ => TRUE) }
  def falseLiteral = positioned { "false" ^^ (_ => FALSE) }
  def defStr = positioned { "def" ^^ (_ => DEF) }
  def endStr = positioned { "end" ^^ (_ => END) }
}