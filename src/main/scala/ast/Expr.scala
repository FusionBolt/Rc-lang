package rclang
package ast

import scala.util.parsing.input.Positional

case class Elsif(cond: Expr, branch: Expr) extends Positional

enum Expr extends Positional:
  case Number(v: Int)
  case Identifier(id: Id)
  case Bool(b: Boolean)
  case Str(str: String)
  case If(cond: Expr, true_branch: Expr, elsif_list: List[Elsif], else_branch: Option[Expr])
  case Lambda(args: List[Expr], stmts: List[Expr])
  case Call(id: Id, args: List[Expr])
