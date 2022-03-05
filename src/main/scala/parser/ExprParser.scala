package rclang
package parser

import ast.*
import lexer.Token.*

import scala.collection.immutable.HashMap
import scala.collection.mutable
import scala.language.postfixOps
import scala.util.parsing.input.Positional

trait BinaryTranslator {
  val opDefaultInfix = HashMap("+"->10, "-"->10, "*"->10, "/"->10, ">"->5, "<"->5)

  def findMaxInfixIndex(terms: List[Positional]): Int =
    terms
      .zipWithIndex
      .filter((x, _) => x.isInstanceOf[OPERATOR])
      .map((x, index) => (x.asInstanceOf[OPERATOR], index))
      .minBy((op, index) => opDefaultInfix(op.op))._2

  def replaceBinaryOp(terms: List[Positional], index: Int): List[Positional] = {
    var t = terms(index)
    val left = terms.slice(0, index - 1)
    val bn = Expr.Binary(
      terms(index).asInstanceOf[OPERATOR].op,
      terms(index - 1).asInstanceOf[Expr],
      terms(index + 1).asInstanceOf[Expr])
    val rights = terms.slice(index + 2, terms.size)
    left.appended(bn).concat(rights)
  }

  def termsToBinary(term: Expr, terms: List[List[Positional]]): Expr = {
    if terms.isEmpty then return term
    termsToBinary(term :: terms.reduce(_.concat(_)))
  }

  def termsToBinary(terms: List[Positional]): Expr = {
    var newTerms = terms
    while (newTerms.size > 1) {
      val max_index = findMaxInfixIndex(newTerms)
      newTerms = replaceBinaryOp(newTerms, max_index)
    }
    newTerms.head.asInstanceOf[Expr.Binary]
  }
}

trait ExprParser extends RcBaseParser with BinaryTranslator {
  def expr: Parser[Expr] = positioned {
    term ~ (operator ~ term).* ^^ {
          // todo:don't known how to write type
      case term ~ terms => termsToBinary(term, terms.map(a => List(a._1, a._2)))
    }
  }

  def string = stringLiteral ^^ { case STRING(str) => Expr.Str(str) }
  def num = number ^^ { case NUMBER(int) => Expr.Number(int) }
  def id = identifier ^^ { case IDENTIFIER(id) => Expr.Identifier(id) }

  def term: Parser[Expr] = positioned {
    bool | num | string | ifExpr | call | id
  }

  def evalExpr: Parser[Expr] = term ~ (operator ~ term).* ^^ {
    case term ~ terms => term
  }

  def bool: Parser[Expr] = positioned {
    TRUE ^^ (_ => Expr.Bool(true)) |
      FALSE ^^ (_ => Expr.Bool(false))
  }

  def ifExpr: Parser[Expr.If] = positioned {
    (IF ~> expr) ~ expr ~ elsif.* ~ (ELSE ~> expr).? ^^ {
      case cond ~ if_branch ~ elsif_list ~ else_branch => Expr.If(cond, if_branch, elsif_list, else_branch)
    }
  }

  def elsif: Parser[Elsif] = positioned {
    (ELSIF ~> expr) ~ expr ^^ {
      case cond ~ branch => Elsif(cond, branch)
    }
  }

  def call: Parser[Expr.Call] = positioned {
    identifier ~ (LEFT_PARENT_THESES ~> repsep(expr, COMMA) <~ RIGHT_PARENT_THESES) ^^ {
      case IDENTIFIER(id) ~ args => Expr.Call(id, args)
    }
  }

}