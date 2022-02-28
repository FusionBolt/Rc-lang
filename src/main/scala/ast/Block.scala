package rclang
package ast

import scala.util.parsing.input.Positional

enum Stmt extends Positional:
  case Local(name: Id, ty: Type)
  case Expr(expr: RcExpr)
  case None

case class Block(stmts: List[Stmt]) extends Positional
