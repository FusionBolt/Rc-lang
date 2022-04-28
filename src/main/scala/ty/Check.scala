package rclang
package ty
import ast.Expr
import ast.Expr.*
import ty.Type.*

case object Check {
  def check(expr: Expr): Boolean = {
    expr match
      case Number(v) => true
      case Identifier(ident) => ???
      case Bool(b) => true
      case Binary(op, lhs, rhs) => lhs == rhs
      case Str(str) => true
      case If(cond, true_branch, false_branch) => cond == Boolean
      case Lambda(args, block) => ???
      case Call(target, args) => ???
      case MethodCall(obj, target, args) => ???
      case Block(stmts) => ???
      case Return(expr) => ???
      case Field(expr, ident) => ???
      case Self => true
      case Constant(ident) => ???
      case Index(expr, i) => ???
  }
}
