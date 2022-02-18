package rclang
package lexer

import scala.util.parsing.input.Positional

enum RcToken extends Positional:
  case IDENTIFIER(str: String)
  case NUMBER(int: Int)
  case OPERATOR(op: String)
  case STRING(str: String)
  case COMMA
  case EQL
  case TRUE
  case FALSE
  case DEF
  case END
  case IF
  case WHILE
  case CLASS
  case SUPER
  case LEFT_PARENT_THESES
  case RIGHT_PARENT_THESES
  case LEFT_SQUARE
  case RIGHT_SQUARE