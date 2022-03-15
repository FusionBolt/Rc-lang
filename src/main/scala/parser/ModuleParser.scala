package rclang
package parser

import ast.*
import lexer.*
import lexer.Token.*
import parser.RcParser

import scala.util.parsing.combinator.Parsers
import scala.util.parsing.input.{NoPosition, Position, Reader}

trait ModuleParser extends RcBaseParser with ExprParser with StmtParser {
  def define: Parser[Item] = method

  def args: Parser[Params] = positioned {
    parSround(repsep(id, COMMA)) ^^ (params => Params(params.map(Param)))
  }

  def method: Parser[Item] = positioned {
    oneline(DEF ~> id ~ args) ~ block <~ END ^^ {
      case id ~ args ~ block => Item.Method(MethodDecl(id, args, Type.Nil), block)
    }
  }

  def item: Parser[Item] = positioned {
    oneline(method | classDefine)
  }

  def module: Parser[RcModule] = positioned {
    item.* ^^ RcModule
  }

  def field: Parser[Field] = positioned {
    VAR ~> (id <~ COLON) ~ sym ~ (EQL ~> expr).? <~ EOL ^^ {
      case id ~ ty ~ value => Field(id, Type.Spec(ty), value)
    }
  }

  def classDefine: Parser[Item.Class] = positioned {
    oneline(CLASS ~> sym ~ (OPERATOR("<") ~> sym).?) ~ (item | field).* <~ END ^^ {
      case klass ~ parent ~ defines =>
        Item.Class(klass, parent,
          defines.filter(_.isInstanceOf[Field]).map(_.asInstanceOf[Field]),
          defines.filter(_.isInstanceOf[Item.Method]).map(_.asInstanceOf[Item.Method]))
    }
  }
}