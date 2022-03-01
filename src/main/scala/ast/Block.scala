package rclang
package ast

import scala.util.parsing.input.Positional

enum Stmt extends Positional:
  case Local(name: Id, ty: Type, value: ast.Expr)
  case Expr(expr: ast.Expr)
  case Return(expr: ast.Expr)
  case None

case class Block(stmts: List[Stmt]) extends Positional
