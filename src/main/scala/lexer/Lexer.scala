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
    continueStr | classStr | superStr | selfStr | varStr | valStr | importStr | forStr | returnStr
  def symbol: Parser[Token] = comment | comma | eol | dot | at | colon | semicolon |
    leftParentTheses | rightParentTheses | leftSquare | rightSquare | leftBracket | rightBracket

  def value: Parser[Token] = number | upperIdentifier | identifier

  def ops = "[+\\-*/%^~!><]|(==)".r
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

  def spacer: Parser[Token] = symbol | eql | operator | space

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

  def NoValueTokenKeyWord(str: String, token: Token): Parser[Token] = positioned {
    str ~ guard(spacer) ^^^ token
  }

  def NoValueTokenWithGuard(str: String, token: Token, guardStr: String): Parser[Token] = positioned {
    str ~ not(guard(guardStr)) ^^^ token
  }

  def NoValueTokenSymbol(str: String, token: Token): Parser[Token] = positioned {
    str ^^^ token
  }

  def comment = NoValueTokenSymbol("#", COMMENT)
  def eol = NoValueTokenSymbol("\n", EOL)
  def eql = NoValueTokenWithGuard("=", EQL, "=")
  def comma = NoValueTokenSymbol(",", COMMA)
  def dot = NoValueTokenSymbol(".", DOT)
  def colon = NoValueTokenSymbol(":", COLON)
  def semicolon = NoValueTokenSymbol(";", SEMICOLON)
  def at = NoValueTokenSymbol("@", AT)

  def trueLiteral = NoValueTokenKeyWord("true", TRUE)
  def falseLiteral = NoValueTokenKeyWord("false", FALSE)

  def varStr = NoValueTokenKeyWord("var", VAR)
  def valStr = NoValueTokenKeyWord("val", VAL)
  def defStr = NoValueTokenKeyWord("def", DEF)
  def returnStr = NoValueTokenKeyWord("return", RETURN)
  def endStr = NoValueTokenKeyWord("end", END)

  def ifStr = NoValueTokenKeyWord("if", IF)
  def thenStr = NoValueTokenKeyWord("then", THEN)
  def elsifStr = NoValueTokenKeyWord("elsif", ELSIF)
  def elseStr = NoValueTokenKeyWord("else", ELSE)
  def whileStr = NoValueTokenKeyWord("while", WHILE)
  def forStr = NoValueTokenKeyWord("for", FOR)
  def breakStr = NoValueTokenKeyWord("break", BREAK)
  def continueStr = NoValueTokenKeyWord("continue", CONTINUE)

  def classStr = NoValueTokenKeyWord("class", CLASS)
  def superStr = NoValueTokenKeyWord("super", SUPER)
  def selfStr = NoValueTokenKeyWord("self", SELF)
  def varsStr = NoValueTokenKeyWord("vars", VARS)
  def methods = NoValueTokenKeyWord("methods", METHODS)

  def importStr = NoValueTokenKeyWord("import", IMPORT)

  def leftParentTheses = NoValueTokenSymbol("(", LEFT_PARENT_THESES)
  def rightParentTheses = NoValueTokenSymbol(")", RIGHT_PARENT_THESES)
  def leftSquare = NoValueTokenSymbol("[", LEFT_SQUARE)
  def rightSquare = NoValueTokenSymbol("]", RIGHT_SQUARE)
  def leftBracket = NoValueTokenSymbol("{", LEFT_BRACKET)
  def rightBracket = NoValueTokenSymbol("}", RIGHT_BRACKET)
}