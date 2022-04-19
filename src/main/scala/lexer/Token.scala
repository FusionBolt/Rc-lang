package rclang
package lexer

import scala.util.parsing.input.Positional

trait Token extends Positional

enum Keyword extends Token:
  // local
  case VAR
  case VAL
  // method
  case DEF
  case RETURN
  case END
  // control flow
  case IF
  case THEN
  case ELSIF
  case ELSE
  case WHILE
  // class
  case CLASS
  case SUPER
  case SELF
  case METHODS
  case VARS

enum Punctuation extends Token:
  case EOL
  case COMMA
  case EQL
  case SPACE
  case DOT
  case COLON
  case AT
  case OPERATOR(op: String)

enum Literal extends Token:
  case NUMBER(int: Int)
  case STRING(str: String)
  case TRUE
  case FALSE

enum Delimiter extends Token:
  case LEFT_PARENT_THESES
  case RIGHT_PARENT_THESES
  case LEFT_SQUARE
  case RIGHT_SQUARE

enum Ident extends Token:
  case IDENTIFIER(str: String)
  case UPPER_IDENTIFIER(str: String)