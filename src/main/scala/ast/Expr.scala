package rclang
package ast

import scala.util.parsing.input.Positional
import ast.Stmt

enum Expr extends Positional:
  case Number(v: Int)
  case Identifier(id: Id)
  case Bool(b: Boolean)
  case Binary(op: String, lhs: Expr, rhs: Expr)
  case Str(str: String)
  // false -> elsif | else
  case If(cond: Expr, true_branch: Block, false_branch: Option[Expr])
  case Lambda(args: List[Expr], stmts: List[Expr])
  case Call(id: Id, args: List[Expr])
  case Block(stmts: List[Stmt])
  case Return(expr: ast.Expr)
