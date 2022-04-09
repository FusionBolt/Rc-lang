package rclang
package ast

import ast.Expr.Block
import ast.Ident
import scala.util.parsing.input.Positional

enum Stmt extends ASTNode:
  case Local(name: Ident, ty: Type, value: ast.Expr)
  case Expr(expr: ast.Expr)
  case While(cond: ast.Expr, stmts: Block)
  case Assign(name: Ident, value: ast.Expr)
