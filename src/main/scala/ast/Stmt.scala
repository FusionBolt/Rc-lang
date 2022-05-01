package rclang
package ast

import ast.Expr.Block
import ast.Ident
import ty.Typed
import scala.util.parsing.input.Positional

enum Stmt extends ASTNode with Typed:
  // todo: value is optional
  case Local(name: Ident, tyInfo: TyInfo, value: ast.Expr)
  case Expr(expr: ast.Expr)
  case While(cond: ast.Expr, body: Block)
  case Assign(name: Ident, value: ast.Expr)
