package rclang
package ast

import scala.util.parsing.input.Positional

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
