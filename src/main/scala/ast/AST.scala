package rclang
package ast

import scala.util.parsing.input.Positional

enum RcAST extends Positional:
  case Expr(expr: RcExpr)

enum BoolConst extends Positional:
  case True
  case False

enum RcExpr extends Positional:
  case Number(v: Int)
  case Identifier(id: String)
  case Bool(b: BoolConst)
  case Str(str: String)
  case If(cond: RcExpr, true_branch: RcExpr, else_branch: RcExpr)
  case While(cond: RcExpr, stmts: Array[RcExpr])
  case Return(expr: RcExpr)
  case Lambda(args: Array[RcExpr], stmts: Array[RcExpr])

enum RcDefine extends Positional:
  case Method