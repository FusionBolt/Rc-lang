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

  def params: Parser[Params] = positioned {
    parSround(repsep(idWithTy, COMMA)) ^^ (params => Params(params.map(Param(_,_))))
  }

  def method: Parser[Item] = positioned {
    oneline(DEF ~> id ~ params) ~ block <~ END ^^ {
      case id ~ params ~ block => Item.Method(MethodDecl(id, params, Type.Nil), block)
    }
  }

  def item: Parser[Item] = positioned {
    oneline(method | classDefine)
  }

  def module: Parser[RcModule] = positioned {
    item.* ^^ RcModule
  }

  def field: Parser[Field] = positioned {
    oneline(VAR ~> (id <~ COLON) ~ sym ~ (EQL ~> expr).?) ^^ {
      case id ~ ty ~ value => Field(id, Type.Spec(ty), value)
    }
  }

  // todo:make a EOL filter
  // todo:make eol test and fix module parser eol problem
  def classDefine: Parser[Item.Class] = positioned {
    oneline(CLASS ~> sym ~ (OPERATOR("<") ~> sym).?) ~ log(item | field | EOL)("class member").* <~ log(END)("class end") ^^ {
      case klass ~ parent ~ defines =>
        Item.Class(klass, parent,
          defines.filter(_.isInstanceOf[Field]).map(_.asInstanceOf[Field]),
          defines.filter(_.isInstanceOf[Item.Method]).map(_.asInstanceOf[Item.Method]))
    }
  }
}