package rclang
package parser

import ast.*
import lexer.Token
import lexer.Token._
import parser.RcBaseParser
import scala.util.parsing.input.{NoPosition, Position}

import scala.util.parsing.combinator.Parsers

trait StmtParser extends RcBaseParser with ExprParser {
  def statement: Parser[Stmt] = positioned {
      assign
      | log(local)("local")
      | ret
      | log(multiLineIf)("multiLineIf") ^^ Stmt.Expr
      | expr ^^ Stmt.Expr
  }

  def multiLineIf: Parser[Expr.If] = positioned {
    // last no eol
    // 1. only if
    // 2. has elsif
    // 3. has else
    // todo:termExpr => expr | stmt
    oneline(IF ~> termExpr) ~ termExpr ~ nextline(elsif).* ~ nextline(ELSE ~> termExpr).? ^^ {
      case cond ~ if_branch ~ elsif_list ~ else_branch => Expr.If(cond, if_branch, elsif_list, else_branch)
    }
  }

  def local: Parser[Stmt] = positioned {
    oneline((VAR ~> identifier) ~ (EQL ~> termExpr)) ^^ {
      case IDENTIFIER(id) ~ expr => Stmt.Local(id, Type.Nil, expr)
    }
  }

  def ret: Parser[Stmt.Return] = positioned {
    RETURN ~> termExpr ^^ Stmt.Return
  }

  def assign: Parser[Stmt.Assign] = positioned {
    (identifier <~ EQL) ~ termExpr ^^ {
      case IDENTIFIER(id) ~ expr => Stmt.Assign(id, expr)
    }
  }
}