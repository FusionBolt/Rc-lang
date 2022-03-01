package rclang
package lexer

import rclang.lexer.Lexer
import rclang.lexer.Token.*
import rclang.RcLexerError
import rclang.Location

import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers

object Lexer extends RegexParsers {
  override def skipWhitespace = false
  override val whiteSpace: Regex = "[ \t\r\f]+".r

  def apply(code: String): Either[RcLexerError, List[Token]] = {
    parse(tokens, code) match {
      case NoSuccess(msg, next) => Left(RcLexerError(Location(next.pos.line, next.pos.column), msg))
      case Success(result, next) => Right(result)
    }
  }

  def keyword: Parser[Token] = stringLiteral | trueLiteral | falseLiteral | defStr | endStr | ifStr | whileStr | eol;
  def value: Parser[Token] = number | identifier

  def ops = "[+\\-*/%^~!]".r
  def operator: Parser[Token] = positioned {
    ops ^^ OPERATOR
  }

  // todo: bracket can no space
  def tokens: Parser[List[Token]] = {
    phrase(repsep(keyword | operator | value, whiteSpace))
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

  def NoValueToken(str: String, token: Token) = positioned {
    str ^^^ token
  }

  def eol = NoValueToken("\n", EOL)
  def eql = NoValueToken("=", EQL)
  def comma = NoValueToken(",", COMMA)

  def trueLiteral = NoValueToken("true", TRUE)
  def falseLiteral = NoValueToken("false", FALSE)

  def varStr = NoValueToken("var", VAR)
  def valStr = NoValueToken("val", VAL)
  def defStr = NoValueToken("def", DEF)
  def returnStr = NoValueToken("return", RETURN)
  def endStr = NoValueToken("end", END)

  def ifStr = NoValueToken("if", IF)
  def elseStr = NoValueToken("else", IF)
  def whileStr = NoValueToken("while", WHILE)

  def classStr = NoValueToken("class", WHILE)
  def superStr = NoValueToken("super", SUPER)

  def leftParentTheses = NoValueToken("(", LEFT_PARENT_THESES)
  def rightParentTheses = NoValueToken(")", LEFT_PARENT_THESES)
  def leftSquare = NoValueToken("[", LEFT_PARENT_THESES)
  def rightSquare = NoValueToken("]", LEFT_PARENT_THESES)
}