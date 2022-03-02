package rclang
package parser

import rclang.ast.*
import rclang.lexer.Token.*

trait ExprParser extends RcBaseParser {
  def expr: Parser[Expr] = positioned {
    bool
      | ifExpr
      | call
      | identifier ^^ { case IDENTIFIER(id) => Expr.Identifier(id) }
      | stringLiteral ^^ { case STRING(str) => Expr.Str(str) }
      | number ^^ { case NUMBER(int) => Expr.Number(int) }
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