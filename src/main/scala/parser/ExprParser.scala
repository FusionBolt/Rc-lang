package rclang
package parser

import rclang.ast.*
import rclang.lexer.RcToken.*

trait ExprParser extends RcParser {
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