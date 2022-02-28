package rclang
package parser

import rclang.ast.*
import rclang.lexer.Token.*

trait ExprParser extends RcBaseParser {
  def expr: Parser[RcExpr] = positioned {
    bool
      | identifier ^^ { case IDENTIFIER(id) => RcExpr.Identifier(id) }
      | stringLiteral ^^ { case STRING(str) => RcExpr.Str(str) }
      | number ^^ { case NUMBER(int) => RcExpr.Number(int) }
  }

  def bool: Parser[RcExpr] = positioned {
    TRUE ^^ (_ => RcExpr.Bool(BoolConst.True)) |
      FALSE ^^ (_ => RcExpr.Bool(BoolConst.False))
  }
}