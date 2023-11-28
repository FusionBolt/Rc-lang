package rclang
package ty
import ast.Expr
import ast.Stmt
import ast.RcModule
import ast.Expr.*
import ast.TyInfo
import ty.*


case object TypeCheck {
  def apply(module: RcModule): Unit = {
    // check(module)
  }

  def check(expr: Expr): Boolean = {
    expr match
      case Number(v) => true
      case Identifier(ident) => ???
      case Bool(b) => true
      case Binary(op, lhs, rhs) => lhs == rhs
      case Str(str) => true
      case If(cond, true_branch, false_branch) => cond == Boolean
      case Lambda(args, block) => ???
      case Call(target, args, _) => ???
      case MethodCall(obj, target, args) => ???
      case Block(stmts) => stmts.forall(stmt => check(stmt))
      case Return(expr) => ???
      case Field(expr, ident) => ???
      case Self => true
      case Symbol(ident, _) => ???
      case Index(expr, i) => ???
  }

  def check(stmt: Stmt): Boolean = {
    stmt match
      case Stmt.Local(name, tyInfo, value) => tyInfo != TyInfo.Nil && check(value)
      case Stmt.Expr(expr) => check(expr)
      case Stmt.While(cond, body) => ???
      case Stmt.Assign(name, value) => ???
  }
}
