package rclang
package parser

import rclang.ast.*
import rclang.lexer.Token.*

import scala.collection.immutable.HashMap
import scala.collection.mutable

trait ExprParser extends RcBaseParser {
  def expr: Parser[Expr] = positioned {
    term
  }

  def string = stringLiteral ^^ { case STRING(str) => Expr.Str(str) }
  def num = number ^^ { case NUMBER(int) => Expr.Number(int) }
  def id = identifier ^^ { case IDENTIFIER(id) => Expr.Identifier(id) }

  def term: Parser[Expr] = positioned {
    bool | num | string | ifExpr |call | id
  }

  def evalExpr: Parser[Expr] = term ~ (operator ~ term).* ^^ {
    case term ~ terms => term
  }

  private val opDefaultInfix = HashMap("+"->10, "-"->10, "*"->10, "/"->10, ">"->5, "<"->5)

  private def findMaxInfixIndex(ops: List[String]) = ops.max(op => opDefaultInfix[op])

  private def termsToBinary(terms: List[(OPERATOR, Expr)]) =
  // 1. find highest op
  // 2. merge

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