package rclang
package parser

import ast.*
import lexer.Token.*

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
      terms(index).asInstanceOf[OPERATOR].op,
      terms(index - 1).asInstanceOf[Expr],
      terms(index + 1).asInstanceOf[Expr])
    val rights = terms.slice(index + 2, terms.size)
    left.appended(bn).concat(rights)
  }

  def termsToBinary(term: Expr, terms: List[List[Positional]]): Expr = {
    if terms.isEmpty then return term
    termsToBinary(term :: terms.reduce(_.concat(_)))
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
          // todo:don't known how to write type
      case term ~ terms => termsToBinary(term, terms.map(a => List(a._1, a._2)))
    }
  }

  def expr: Parser[Expr] = positioned {
    multiLineIf | termExpr | ret
  }

  def string = stringLiteral ^^ { case STRING(str) => Expr.Str(str) }
  def num = number ^^ { case NUMBER(int) => Expr.Number(int) }
  def id = identifier ^^ { case IDENTIFIER(id) => Expr.Identifier(id) }

  def term: Parser[Expr] = positioned {
    bool | num | string | call | id
  }

  def evalExpr: Parser[Expr] = term ~ (operator ~ term).* ^^ {
    case term ~ terms => term
  }

  def bool: Parser[Expr] = positioned {
    TRUE ^^ (_ => Expr.Bool(true)) |
      FALSE ^^ (_ => Expr.Bool(false))
  }

  def call: Parser[Expr.Call] = positioned {
    identifier ~ (LEFT_PARENT_THESES ~> repsep(termExpr, COMMA) <~ RIGHT_PARENT_THESES) ^^ {
      case IDENTIFIER(id) ~ args => Expr.Call(id, args)
    }
  }

  def block: Parser[Block] = positioned {
    repsep(log(statement)("stmt"), EOL.*) ^^ (stmts => Block(stmts))
  }

  def multiLineIf: Parser[If] = positioned {
    // last no eol
    // 1. only if
    // 2. has elsif
    // 3. has else
    // todo:termExpr => expr | stmt
    oneline(IF ~> expr) ~ block ~ elsifs ~ nextline(ELSE ~> block).? ^^ {
      case cond ~ if_branch ~ elsif ~ else_branch => If(cond, if_branch, else_branch)
    }
  }

  def elsif: Parser[If] = positioned {
    oneline(ELSIF ~> termExpr) ~ block ^^ {
      case cond ~ branch => If(cond, branch, None)
    }
  }

  def elsifs: Parser[Option[If]] = positioned {
    // todo:monad?
    nextline(elsif).* ^^ {
      case elsif => if elsif.isEmpty then None else Some(elsif.reduce((l, r) => If(l.cond, l.true_branch, Some(r))))
//      case elsif => elsif.reduce((l, r) => If(l.cond, l.true_branch, Some(r)))
    }
  }

  def statement: Parser[Stmt] = positioned {
    assign
      | log(local)("local")
      | expr ^^ Stmt.Expr
  }

  def local: Parser[Stmt] = positioned {
    oneline((VAR ~> identifier) ~ (EQL ~> termExpr)) ^^ {
      case IDENTIFIER(id) ~ expr => Stmt.Local(id, Type.Nil, expr)
    }
  }

  def ret: Parser[Return] = positioned {
    RETURN ~> termExpr ^^ Return
  }

  def assign: Parser[Stmt.Assign] = positioned {
    (identifier <~ EQL) ~ termExpr ^^ {
      case IDENTIFIER(id) ~ expr => Stmt.Assign(id, expr)
    }
  }
}