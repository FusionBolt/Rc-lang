package rclang
package lexer

import scala.util.parsing.input.Positional

enum RcToken extends Positional:
  case IDENTIFIER(str: String)
  case NUMBER(int: Int)
  case TRUE
  case FALSE
  case STRING(str: String)
  case DEF
  case END