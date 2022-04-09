package rclang
package parser

import lexer.Keyword.*
import lexer.Punctuation.*
import lexer.Literal.*
import lexer.Delimiter.*
import lexer.Ident.*
import parser.RcParser
import ast.*

import scala.util.parsing.combinator.Parsers
import scala.util.parsing.input.{NoPosition, Position, Reader}

trait ModuleParser extends RcBaseParser with ExprParser {
  def define: Parser[Item] = method

  def params: Parser[Params] = positioned {
    parSround(repsep(idWithTy, COMMA)) ^^ (params => Params(params.map(Param(_,_))))
  }

  def method: Parser[Item] = positioned {
    oneline(DEF ~> id ~ params) ~ block <~ END ^^ {
      case id ~ params ~ block => Item.Method(MethodDecl(id, params, Type.Nil), block)
    }
  }

  // noneItem should be same level as oneline item
  // todo: add test
  def item: Parser[Item] = positioned {
    oneline(method | classDefine)
  }

  def module: Parser[RcModule] = positioned {
    (item | noneItem).* ^^ { items => RcModule(items.filter(_ != Empty).map(_.asInstanceOf[Item])) }
  }

  def field: Parser[FieldDef] = positioned {
    oneline(VAR ~> (id <~ COLON) ~ sym ~ (EQL ~> expr).?) ^^ {
      case id ~ ty ~ value => FieldDef(id, Type.Spec(ty), value)
    }
  }

  def noneItem: Parser[ASTNode] = positioned {
    EOL ^^^ Empty
  }

  // todo:refactor
  // todo:make a EOL filter
  def classDefine: Parser[Item.Class] = positioned {
    oneline(CLASS ~> sym ~ (OPERATOR("<") ~> sym).?) ~ log(item | field | noneItem)("class member").* <~ log(END)("class end") ^^ {
      case klass ~ parent ~ defines =>
        Item.Class(klass, parent,
          defines.filter(_.isInstanceOf[FieldDef]).map(_.asInstanceOf[FieldDef]),
          defines.filter(_.isInstanceOf[Item.Method]).map(_.asInstanceOf[Item.Method]))
    }
  }
}