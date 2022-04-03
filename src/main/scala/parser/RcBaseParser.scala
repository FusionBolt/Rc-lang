package rclang
package parser

import lexer.Token.*

import lexer.Token
import ast.Id
import ast.Type

import scala.util.parsing.combinator.PackratParsers
import scala.util.parsing.input.{CharSequenceReader, NoPosition, Position, Positional, Reader}

trait RcBaseParser extends PackratParsers {
  override type Elem = Token

  private def identifier: Parser[IDENTIFIER] = positioned {
    accept("identifier", { case id@IDENTIFIER(name) => id })
  }

  private def upperIdentifier: Parser[UPPER_IDENTIFIER] = positioned {
    accept("upper_identifier", { case id@UPPER_IDENTIFIER(name) => id })
  }

  protected def id: Parser[Id] = positioned {
    identifier ^^ { case IDENTIFIER(id) => Id(id) }
  }

  protected def sym: Parser[Id] = positioned {
    upperIdentifier ^^ { case UPPER_IDENTIFIER(id) => Id(id) }
  }

  protected def stringLiteral: Parser[STRING] = positioned {
    accept("string literal", { case lit@STRING(str) => lit })
  }

  protected def number: Parser[NUMBER] = positioned {
    accept("number literal", { case num@NUMBER(n) => num })
  }

  protected def operator: Parser[OPERATOR] = positioned {
    accept("operator", { case op@OPERATOR(_) => op })
  }

  protected def idWithTy: Parser[(Id, Type)] = {
    id ~ (COLON ~> ty).? ^^ {
      case id ~ ty => (id, ty.getOrElse(Type.Infer))
    }
  }

  protected def ty: Parser[Type] = positioned {
    sym ^^ Type.Spec
  }

  protected def oneline[T](p: Parser[T]): Parser[T] = log(p <~ EOL)("oneline")

  protected def onelineOpt[T](p: Parser[T]): Parser[T] = log(p <~ EOL.?)("oneline")

  protected def nextline[T](p: Parser[T]): Parser[T] = log(EOL ~> p)("nextline")

  // parenthesesSround
  protected def parSround[T](p: Parser[T]) = LEFT_PARENT_THESES ~> p <~ RIGHT_PARENT_THESES

  protected def squareSround[T](p: Parser[T]) = LEFT_SQUARE ~> p <~ RIGHT_SQUARE

  protected def noOrder[T](p1: Parser[T], p2: Parser[T]): Parser[T ~ T] = p1 ~ p2 | p2 ~ p1

  protected def makeParserError(next: Input, msg: String) = RcParserError(Location(next.pos.line, next.pos.column), msg)

  protected def doParser[T](tokens: Seq[Token], parser: Parser[T]): Either[RcParserError, T] = {
    doParserImpl(tokens, parser).map(_._1)
  }

  protected def doParserImpl[T](tokens: Seq[Token], parser: Parser[T]): Either[RcParserError, (T, Input)] = {
    val reader = new RcPackratReader(new RcTokenReader(tokens))
    parser(reader) match {
      case NoSuccess(msg, next) => Left(makeParserError(next, msg))
      case Success(result, next) => Right(result, next)
    }
  }

  def noEmptyEval[T](l: List[T], f: List[T] => List[T], els: List[T] = List()) = if l.isEmpty then els else f(l)

  class RcTokenReader(tokens: Seq[Token]) extends Reader[Token] {
    override def first: Token = tokens.head

    override def atEnd: Boolean = tokens.isEmpty

    override def pos: Position = tokens.headOption.map(_.pos).getOrElse(NoPosition)

    override def rest: Reader[Token] = new RcTokenReader(tokens.tail)

    override def toString: String = {
      val c = if (atEnd) "" else s"${tokens.slice(0, 3)} ..."
      s"RcTokenReader($c)"
    }
  }

  class RcPackratReader(reader: Reader[Token])  extends PackratReader[Token](reader) {
    override def toString: String = {
      reader.toString
    }
  }
}