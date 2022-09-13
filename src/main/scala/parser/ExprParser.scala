package rclang
package parser

import ast.*
import lexer.Keyword.*
import lexer.Punctuation.*
import lexer.Literal.*
import lexer.Delimiter.*
import lexer.Ident.*
import lexer.Token
import ast.Expr.{Block, If, Return}

import scala.collection.immutable.HashMap
import scala.collection.mutable
import scala.language.postfixOps
import scala.util.parsing.input.Positional

trait BinaryTranslator {
  val opDefaultInfix = HashMap("+"->10, "-"->10, "*"->10, "/"->10, ">"->5, "<"->5)

  def findMaxInfixIndex(terms: List[Positional]): Int =
    terms
      .zipWithIndex
      .filter((x, _) => x.isInstanceOf[OPERATOR])
      .map((x, index) => (x.asInstanceOf[OPERATOR], index))
      .minBy((op, index) => opDefaultInfix(op.op))._2

  def replaceBinaryOp(terms: List[Positional], index: Int): List[Positional] = {
    var t = terms(index)
    val left = terms.slice(0, index - 1)
    val bn = Expr.Binary(
      strToOp(terms(index).asInstanceOf[OPERATOR].op),
      terms(index - 1).asInstanceOf[Expr],
      terms(index + 1).asInstanceOf[Expr])
    val rights = terms.slice(index + 2, terms.size)
    left.appended(bn):::(rights)
  }

  def termsToBinary(term: Expr, terms: List[List[Positional]]): Expr = {
    if terms.isEmpty then return term
    termsToBinary(term :: terms.reduce(_:::_))
  }

  def termsToBinary(terms: List[Positional]): Expr = {
    var newTerms = terms
    while (newTerms.size > 1) {
      val max_index = findMaxInfixIndex(newTerms)
      newTerms = replaceBinaryOp(newTerms, max_index)
    }
    newTerms.head.asInstanceOf[Expr.Binary]
  }
}

trait ExprParser extends RcBaseParser with BinaryTranslator {
  def termExpr: Parser[Expr] = positioned {
    term ~ (operator ~ term).* ^^ {
      case term ~ terms => termsToBinary(term, terms.map(a => List(a._1, a._2)))
    }
  }

  def expr: Parser[Expr] = positioned {
    multiLineIf | termExpr | ret
  }

  def string = stringLiteral ^^ { case STRING(str) => Expr.Str(str) }
  def num = number ^^ { case NUMBER(int) => Expr.Number(int) }
  def idExpr = id ^^ Expr.Identifier

  // memField: term.x
  // memCall: term.x(
  // arrayIndex: term[
  lazy val beginWithTerm: PackratParser[Expr] = positioned {
    memCall | memField | arrayIndex
  }

  def term: Parser[Expr] = positioned {
    bool | num | string | selfField | call | beginWithTerm | sym ^^ Expr.Symbol | idExpr
  }

  def bool: Parser[Expr] = positioned {
    TRUE ^^ (_ => Expr.Bool(true)) |
      FALSE ^^ (_ => Expr.Bool(false))
  }

  def call: Parser[Expr.Call] = positioned {
    id ~ parSround(repsep(termExpr, COMMA)) ^^ {
      case id ~ args => Expr.Call(id, args)
    }
  }

  def selfField: Parser[Expr.Field] = positioned {
    (AT ~> id) ^^ (id => Expr.Field(Expr.Self, id))
  }

  def memField: Parser[Expr.Field] = positioned {
    log(termExpr <~ DOT)("MemberLog") ~ log(id)("FieldLog") ^^ {
      case obj ~ name => Expr.Field(obj, name)
    }
  }

  def memCall: Parser[Expr.MethodCall] = positioned {
    (termExpr <~ DOT) ~ id ~ parSround(repsep(termExpr, COMMA)) ^^ {
      case obj ~ id ~ args => Expr.MethodCall(obj, id, args)
    }
  }

  def arrayIndex: Parser[Expr.Index] = positioned {
    termExpr ~ squareSround(termExpr) ^^ {
      case expr ~ index => Expr.Index(expr, index)
    }
  }
  
  def block: Parser[Block] = positioned {
    rep(log(statement | none)("stmt")) ^^ (stmts => Block(stmts.filter(_ != Empty).map(_.asInstanceOf[Stmt])))
  }

  def multiLineIf: Parser[If] = positioned {
    oneline(IF ~> expr) ~ block ~ log(elsif.*)("elsif") ~ (oneline(ELSE) ~> log(block)("else block")).? <~ log(END)("end") ^^ {
      case cond ~ if_branch ~ elsif ~ else_branch
      => If(cond, if_branch, elsif.foldRight(else_branch.asInstanceOf[Option[Expr]])(
        (next, acc) => Some(If(next.cond, next.true_branch, acc))))
    }
  }

  def elsif: Parser[If] = positioned {
    oneline(ELSIF ~> termExpr) ~ block ^^ {
      case cond ~ branch => If(cond, branch, None)
    }
  }

  def statement: Parser[Stmt] = positioned {
    oneline(assign
      | whileStmt
      | log(local)("local")
      | BREAK ^^^ Stmt.Break()
      | CONTINUE ^^^ Stmt.Continue()
      | expr ^^ Stmt.Expr)
  }

  def none: Parser[ASTNode] = positioned {
    EOL ^^^ Empty
  }

  def local: Parser[Stmt] = positioned {
    ((VAR | VAL) ~> id) ~ (EQL ~> termExpr) ^^ {
      case id ~ expr => Stmt.Local(id, TyInfo.Infer, expr)
    }
  }

  def ret: Parser[Return] = positioned {
    RETURN ~> termExpr ^^ Return
  }

  def assign: Parser[Stmt.Assign] = positioned {
    (id <~ EQL) ~ termExpr ^^ {
      case id ~ expr => Stmt.Assign(id, expr)
    }
  }

  def whileStmt: Parser[Stmt.While] = positioned {
    oneline(WHILE ~> parSround(termExpr)) ~ block <~ log(END)("end while") ^^ {
      case cond ~ body => Stmt.While(cond, body)
    }
  }
}

object RcExprParser extends ExprParser {
  def apply(tokens: Seq[Token]) : Either[RcParserError, Stmt] = {
    doParser(tokens, statement)
  }
}
