package rclang
package ast

import scala.util.parsing.input.Positional
import ast.Stmt
import ast.Ident

import BinaryOp.*
import ty.Typed
import ty.Type
import ty.Infer

enum BinaryOp(op: String) extends Positional:
  case Add extends BinaryOp("+")
  case Sub extends BinaryOp("-")
  case Mul extends BinaryOp("*")
  case Div extends BinaryOp("/")
  case EQ extends BinaryOp("==")
  case LT extends BinaryOp("<")
  case GT extends BinaryOp(">")

def strToOp(op: String): BinaryOp = {
  op match {
    case "+" => Add
    case "-" => Sub
    case "*" => Mul
    case "/" => Div
    case "==" => EQ
    case "<" => LT
    case ">" => GT
  }
}

enum Expr extends ASTNode with Typed:
  case Number(v: Int)
  case Identifier(ident: Ident)
  case Bool(b: Boolean)
  case Binary(op: BinaryOp, lhs: Expr, rhs: Expr)
  case Str(str: String)
  // false -> elsif | else
  case If(cond: Expr, true_branch: Block, false_branch: Option[Expr])
  case Lambda(args: Params, block: Block)
  // todo:call a lambda
  case Call(target: Ident, args: List[Expr])
  case MethodCall(obj: Expr, target: Ident, args: List[Expr])
  case Block(stmts: List[Stmt])
  case Return(expr: ast.Expr)
  case Field(expr: Expr, ident: Ident)
  case Self
  // symbol
  case Constant(ident: Ident)
  case Index(expr: Expr, i: Expr)
