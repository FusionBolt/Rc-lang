package rclang
package ast

import scala.util.parsing.input.Positional
import ast.Stmt
import ast.Id

enum Expr extends Positional:
  case Number(v: Int)
  case Identifier(id: Id)
  case Bool(b: Boolean)
  case Binary(op: String, lhs: Expr, rhs: Expr)
  case Str(str: String)
  // false -> elsif | else
  case If(cond: Expr, true_branch: Block, false_branch: Option[Expr])
  case Lambda(args: List[Expr], stmts: List[Expr])
  case Call(target: Id, args: List[Expr])
  case MethodCall(obj: Expr, target: Id, args: List[Expr])
  case Block(stmts: List[Stmt])
  case Return(expr: ast.Expr)
  case Field(expr: Expr, id: Id)
  case Self
  case Constant(id: Id)
  case Index(expr: Expr, i: Expr)