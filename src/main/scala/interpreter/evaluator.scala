package rclang
package interpreter

import ast.*
import ast.Expr.*
import cats.implicits._

case class Evaluator(var fenv:Map[Ident, Method] = Map()) {
  var env = Map[Ident, Expr]()

  def run_call(target: Ident, args: List[Expr]): Any = {
    run_call_impl(fenv(target), args)
  }

  def run_call_impl(method: Method, args: List[Expr]) : Any = {
    val new_env = method.decl.inputs.params.zip(args).map{case (p, a) => (p.name, a)}.toMap
    env = env ++ new_env
    run_expr(method.body)
  }

  def run_module(mod: RcModule): Any = {

  }

  def run_expr(expr: Expr): Any = {
    expr match
      case Number(v) => v
      case Identifier(ident) => run_expr(env(ident))
      case Bool(b) => b
      case Binary(op, lhs, rhs) => ???
      case Str(str) => str
      case If(cond, true_branch, false_branch) => run_expr(cond) match {
        case Bool(true) => run_expr(true_branch)
        case Bool(false) => false_branch // todo:fix this
      }
      case Lambda(args, block) => ???
      case Call(target, args) => ???
      case MethodCall(obj, target, args) => ???
      case Block(stmts) => stmts.map(run_stmt).last
      case Return(expr) => run_expr(expr)
      case Field(expr, ident) => ???
      case Self => ???
      case Constant(ident) => ???
      case Index(expr, i) => ???
  }

  def run_stmt(stmt: Stmt) = {
    stmt match
      case Stmt.Local(name, tyInfo, value) => ???
      case Stmt.Expr(expr) => ???
      case Stmt.While(cond, body) => ???
      case Stmt.Assign(name, value) => ???
  }
}
