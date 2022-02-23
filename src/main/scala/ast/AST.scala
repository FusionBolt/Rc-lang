package rclang
package ast

import scala.util.parsing.input.Positional

type Id = String

enum RcAST extends Positional:
  case Expr(expr: RcExpr)
  case Define(define: RcItem)

enum BoolConst extends Positional:
  case True
  case False

enum RcExpr extends Positional:
  case Number(v: Int)
  case Identifier(id: Id)
  case Bool(b: BoolConst)
  case Str(str: String)
  case If(cond: RcExpr, true_branch: RcExpr, else_branch: RcExpr)
  case While(cond: RcExpr, stmts: List[RcExpr])
  case Return(expr: RcExpr)
  case Lambda(args: List[RcExpr], stmts: List[RcExpr])

enum Type extends Positional:
  case Nil

case class MethodSignature() extends Positional
case class Param(name: Id) extends Positional
case class Params(params: List[Param]) extends Positional
case class MethodDecl(name: Id, inputs: Params, output: Type) extends Positional
case class Block(stmts: List[Statement]) extends Positional

enum RcItem extends Positional:
  case Method(decl: MethodDecl, body: Block)
  case Class

enum Statement extends Positional:
  case Local(name: Id, ty: Type)
  case Expr(expr: RcExpr)
  case None