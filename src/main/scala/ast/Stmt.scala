package rclang
package ast

import ast.Expr.Block
import ast.Id
import scala.util.parsing.input.Positional

enum Stmt extends Positional:
  case Local(name: Id, ty: Type, value: ast.Expr)
  case Expr(expr: ast.Expr)
  case While(cond: ast.Expr, stmts: Block)
  case Assign(name: Id, value: ast.Expr)
  case None
