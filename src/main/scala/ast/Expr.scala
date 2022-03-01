package rclang
package ast

import scala.util.parsing.input.Positional

enum BoolConst extends Positional:
  case True
  case False

enum Expr extends Positional:
  case Number(v: Int)
  case Identifier(id: Id)
  case Bool(b: BoolConst)
  case Str(str: String)
  case If(cond: Expr, true_branch: Expr, else_branch: Expr)
  case While(cond: Expr, stmts: List[Expr])
  case Lambda(args: List[Expr], stmts: List[Expr])
