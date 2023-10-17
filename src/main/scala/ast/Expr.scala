package rclang
package ast

import scala.util.parsing.input.Positional
import ast.Stmt
import ast.Ident

import BinaryOp.*
import ty.Typed
import ty.Type
import ty.Infer
import ast.ImplicitConversions.strToId

def uuid = java.util.UUID.randomUUID.toString

def lambdaToMethod(lambda: Expr.Lambda): Method = lambdaToMethod(lambda.args, lambda.block)

def lambdaToMethod(args: Params, body: Expr): Method = {
  val blockBody: Expr.Block = body match
    case b: Expr.Block => b
    case _ => Expr.Block(List(Stmt.Expr(body)))
  Method(MethodDecl(s"lambda_${uuid}", args, TyInfo.Infer), blockBody)
}

enum BinaryOp(op: String) extends Positional :
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

enum Expr extends ASTNode with Typed :
  case Number(v: Int)
  case Identifier(ident: Ident)
  case Bool(b: Boolean)
  case Binary(op: BinaryOp, lhs: Expr, rhs: Expr)
  case Str(str: String)
  // false -> elsif | else
  case If(cond: Expr, true_branch: Block, false_branch: Option[Expr])
  case Lambda(args: Params, block: Block)
  case Call(target: Ident, args: List[Expr], generic: Option[Ident] = None)
  case MethodCall(obj: Expr, target: Ident, args: List[Expr])
  case Block(stmts: List[Stmt])
  case Return(expr: ast.Expr)
  case Field(expr: Expr, ident: Ident)
  case Self
  // symbol
  case Symbol(ident: Ident, generic: Option[Ident] = None)
  case Index(expr: Expr, i: Expr)
  case Array(len: Int, initValues: List[Expr])

  override def toString: String = this match
    case Expr.Block(stmts) => s"{\n${stmts.mkString("\n")}\n}"
    case _ => s"${super.toString}:${ty}"