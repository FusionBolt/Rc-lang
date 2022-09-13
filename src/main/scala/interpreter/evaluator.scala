package rclang
package interpreter

import ast.*
import ast.BinaryOp.*
import ast.Expr.*

import cats.implicits.*
import analysis.SymScanner
import tools.GlobalTable
import ty.{TyCtxt, TypedTranslator}
import ast.ImplicitConversions.strToId

case class Structure(var name: String, var fields: List[String], var values: List[Expr]) {

}

case class Evaluator(var fenv:Map[Ident, Method] = Map()) {
  var env = Map[Ident, Expr]()
  var curObj: Structure = null


  def run_call(target: Ident, args: List[Expr]): Any = {
    run_call_impl(fenv(target), args)
  }

  def run_call_impl(method: Method, args: List[Expr]) : Any = {
    val new_env = method.decl.inputs.params.zip(args).map{case (p, a) => (p.name, a)}.toMap
    env = env ++ new_env
    run_expr(method.body)
  }

  def run_module(mod: RcModule): Any = {
    val table = SymScanner(mod)
    val tyCtxt = TyCtxt()
    tyCtxt.setGlobalTable(table)
    val typedModule = TypedTranslator(tyCtxt)(mod)
    run_call("main", List())
  }

  def run_expr_t[T](expr: Expr): T = {
    ???
//    run_expr(expr).asInstanceOf[T]
  }

  def run_expr(expr: Expr): Any = {
    expr match
      case Number(v) => v
      case Identifier(ident) => run_expr(env(ident))
      case Bool(b) => b
      case Binary(op, lhs, rhs) => {
        ???
        val l = run_expr_t[Int](lhs)
        val r = run_expr_t[Int](rhs)
        op match
          case Add => l + r
          case Sub => l - r
          case Mul => l * r
          case Div => l / r
          case EQ => l == r
          case LT => l < r
          case GT => l > r
      }
      case Str(str) => str
      case If(cond, true_branch, false_branch) => run_expr(cond) match {
        case Bool(true) => run_expr(true_branch)
        case Bool(false) => ???
      }
      case Lambda(args, block) => ???
      case Call(target, args) => run_call(target, args)
      case MethodCall(obj, target, args) => ???
      case Block(stmts) => stmts.map(run_stmt).last
      case Return(expr) => run_expr(expr)
      case Field(expr, ident) => ???
      case Self => ???
      case Symbol(ident) => ???
      case Index(expr, i) => ???
  }

  def run_stmt(stmt: Stmt) = {
    stmt match
      case Stmt.Local(name, tyInfo, value) => env = env.updated(name, value)
      case Stmt.Expr(expr) => run_expr(expr)
      case Stmt.While(cond, body) => while(run_expr_t[Boolean](cond)) run_expr(body)
      case Stmt.Assign(name, value) => env = env.updated(name, value)
  }
}
