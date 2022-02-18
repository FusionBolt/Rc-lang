package rclang
package lexer

import rclang.lexer.RcLexer
import rclang.lexer.RcToken.*
import rclang.RcLexerError
import rclang.Location

import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers

object RcLexer extends RegexParsers {
  override def skipWhitespace = false
  override val whiteSpace: Regex = "[ \t\r\f]+".r

  def apply(code: String): Either[RcLexerError, List[RcToken]] = {
    parse(tokens, code) match {
      case NoSuccess(msg, next) => Left(RcLexerError(Location(next.pos.line, next.pos.column), msg))
      case Success(result, next) => Right(result)
    }
  }

  def keyword: Parser[RcToken] = stringLiteral | trueLiteral | falseLiteral | defStr | endStr | ifStr | whileStr;
  def value: Parser[RcToken] = number | identifier

  def operator: Regex = "[+\\-*/^%]".r

  // todo: bracket can no space
  def tokens: Parser[List[RcToken]] = {
    phrase(repsep(keyword | value, whiteSpace))
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
  def ifStr = positioned { "if" ^^ (_ => IF) }
  def whileStr = positioned { "while" ^^ (_ => WHILE) }
  def classStr = positioned { "class" ^^ (_ => WHILE) }
  def superStr = positioned { "super" ^^ (_ => WHILE) }

  def eql = positioned { "=" ^^ (_ => EQL) }
  def comma = positioned { "," ^^ (_ => COMMA) }

  def leftParentTheses = positioned { "(" ^^ (_ => LEFT_PARENT_THESES) }
  def rightParentTheses = positioned { ")" ^^ (_ => LEFT_PARENT_THESES) }
  def leftSquare = positioned { "[" ^^ (_ => LEFT_PARENT_THESES) }
  def rightSquare = positioned { "]" ^^ (_ => LEFT_PARENT_THESES) }
}