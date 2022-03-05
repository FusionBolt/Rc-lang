package rclang
package lexer

import scala.util.parsing.input.Positional

enum Token extends Positional:
  case IDENTIFIER(str: String)
  case NUMBER(int: Int)
  case OPERATOR(op: String)
  case STRING(str: String)
  
  case EOL
  case COMMA
  case EQL
  case SPACE

  case TRUE
  case FALSE

  case VAR
  case VAL

  case DEF
  case RETURN
  case END

  case IF
  case ELSIF
  case ELSE
  case WHILE

  case CLASS
  case SUPER

  case LEFT_PARENT_THESES
  case RIGHT_PARENT_THESES
  case LEFT_SQUARE
  case RIGHT_SQUARE