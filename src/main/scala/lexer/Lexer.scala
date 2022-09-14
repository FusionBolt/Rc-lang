package rclang
package lexer

import lexer.*
import lexer.Keyword.*
import lexer.Punctuation.*
import lexer.Literal.*
import lexer.Delimiter.*
import lexer.Ident.*
import rclang.RcLexerError
import rclang.Location

import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers

object Lexer extends RegexParsers {
  override def skipWhitespace = false
  override val whiteSpace: Regex = "[ \t\r\f]+".r

  def eliminateComment(src: String) = {
    val end = if(src.endsWith("\n")) then "\n" else ""
    (src.split("\n").map(c => {
      val begin = c.indexOf("#")
      if (begin != -1) {
        c.slice(0, begin)
      } else {
        c
      }
    }).mkString("\n")) + end
  }
  def apply(originSrc: String): Either[RcLexerError, List[Token]] = {
    val code = eliminateComment(originSrc)
    parse(tokens, code) match {
      case NoSuccess(msg, next) => Left(RcLexerError(Location(next.pos.line, next.pos.column), msg))
      case Success(result, next) => Right(result)
    }
  }

  def keyword: Parser[Token] = stringLiteral | trueLiteral | falseLiteral |
    defStr | endStr | ifStr | thenStr | elsifStr | elseStr | whileStr | breakStr |
    continueStr | classStr | superStr | selfStr | varStr | valStr
  def symbol: Parser[Token] = comment | comma | eol | dot | at | colon |
    leftParentTheses | rightParentTheses | leftSquare | rightSquare

  def value: Parser[Token] = number | upperIdentifier | identifier

  def ops = "[+\\-*/%^~!><]".r
  def operator: Parser[Token] = positioned {
    ops ^^ OPERATOR
  }

  def tokens: Parser[List[Token]] = {
    phrase(allTokens)
  }

  def rep1sepNoDis[T](p : => Parser[T], q : => Parser[Any]): Parser[List[T]] =
    p ~ rep(q ~ p) ^^ {case x~y => x::y.map(x => List(x._1.asInstanceOf[T], x._2)).fold(List())(_:::_)}

  def allTokens: Parser[List[Token]] = {
    ((rep1sepNoDis(repN(1, notSpacer), spacer.+) ~ spacer.*) |
      // BAA is imposible
      (rep1sepNoDis(spacer.+, repN(1, notSpacer)) ~ notSpacer.?)) ^^ {
      case list ~ t =>
        list
          .fold(List())(_:::_)
          .concat(t match {
            case Some(v) => List(v)
            case None => List()
            case _ => t
          })
          .filter(_ != SPACE)
    }
  }

  def space: Parser[Token] = positioned {
    whiteSpace.+ ^^^ SPACE
  }

  def notSpacer: Parser[Token] = keyword | value | eol

  def spacer: Parser[Token] = symbol | operator | eql | space

  def upperIdentifier: Parser[UPPER_IDENTIFIER] = positioned {
    "[A-Z_][a-zA-Z0-9_]*".r ^^ { str => UPPER_IDENTIFIER(str) }
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

  def NoValueToken(str: String, token: Token): Parser[Token] = positioned {
    str ^^^ token
  }

  def comment = NoValueToken("#", COMMENT)
  def eol = NoValueToken("\n", EOL)
  def eql = NoValueToken("=", EQL)
  def comma = NoValueToken(",", COMMA)
  def dot = NoValueToken(".", DOT)
  def colon = NoValueToken(":", COLON)
  def at = NoValueToken("@", AT)

  def trueLiteral = NoValueToken("true", TRUE)
  def falseLiteral = NoValueToken("false", FALSE)

  def varStr = NoValueToken("var", VAR)
  def valStr = NoValueToken("val", VAL)
  def defStr = NoValueToken("def", DEF)
  def returnStr = NoValueToken("return", RETURN)
  def endStr = NoValueToken("end", END)

  def ifStr = NoValueToken("if", IF)
  def thenStr = NoValueToken("then", THEN)
  def elsifStr = NoValueToken("elsif", ELSIF)
  def elseStr = NoValueToken("else", ELSE)
  def whileStr = NoValueToken("while", WHILE)
  def breakStr = NoValueToken("break", BREAK)
  def continueStr = NoValueToken("continue", CONTINUE)

  def classStr = NoValueToken("class", CLASS)
  def superStr = NoValueToken("super", SUPER)
  def selfStr = NoValueToken("self", SELF)
  def varsStr = NoValueToken("vars", VARS)
  def methods = NoValueToken("methods", METHODS)

  def leftParentTheses = NoValueToken("(", LEFT_PARENT_THESES)
  def rightParentTheses = NoValueToken(")", RIGHT_PARENT_THESES)
  def leftSquare = NoValueToken("[", LEFT_SQUARE)
  def rightSquare = NoValueToken("]", RIGHT_SQUARE)
}