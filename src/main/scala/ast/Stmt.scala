package rclang
package ast

import scala.util.parsing.input.Positional

enum Stmt extends Positional:
  case Local(name: Id, ty: Type, value: ast.Expr)
  case Expr(expr: ast.Expr)
  case While(cond: Expr, stmts: List[Expr])
  case Assign(name: Id, value: ast.Expr)
  case None
