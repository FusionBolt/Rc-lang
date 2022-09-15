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
      case id ~ params ~ block => Method(MethodDecl(id, params, TyInfo.Infer), block)
    }
  }

  // noneItem should be same level as oneline item
  def item: Parser[Item] = positioned {
    oneline(method | classDefine)
  }

  def module: Parser[RcModule] = positioned {
    (item | noneItem).* ^^ { items => RcModule(items.filter(_ != Empty).map(_.asInstanceOf[Item])) }
  }

  def field: Parser[FieldDef] = positioned {
    oneline(VAR ~> (id <~ COLON) ~ sym ~ (EQL ~> expr).?) ^^ {
      case id ~ ty ~ value => FieldDef(id, TyInfo.Spec(ty), value)
    }
  }

  def noneItem: Parser[ASTNode] = positioned {
    EOL ^^^ Empty
  }

  def classDefine: Parser[Item] = positioned {
    oneline(CLASS ~> sym ~ (OPERATOR("<") ~> sym).?) ~ log(item | field | noneItem)("class member").* <~ log(END)("class end") ^^ {
      case klass ~ parent ~ defines =>
        Class(klass, parent,
          defines.filter(_.isInstanceOf[FieldDef]).map(_.asInstanceOf[FieldDef]),
          defines.filter(_.isInstanceOf[Method]).map(_.asInstanceOf[Method])).asInstanceOf[Item]
    }
  }
}