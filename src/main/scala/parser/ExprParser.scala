package rclang
package parser

import rclang.ast.*
import rclang.lexer.Token.*

trait ExprParser extends RcBaseParser {
  def expr: Parser[Expr] = positioned {
    bool
      | identifier ^^ { case IDENTIFIER(id) => Expr.Identifier(id) }
      | stringLiteral ^^ { case STRING(str) => Expr.Str(str) }
      | number ^^ { case NUMBER(int) => Expr.Number(int) }
  }

  def bool: Parser[Expr] = positioned {
    TRUE ^^ (_ => Expr.Bool(BoolConst.True)) |
      FALSE ^^ (_ => Expr.Bool(BoolConst.False))
  }
}