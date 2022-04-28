package rclang
package ty
import ast.Expr.*
import ast.*
import ty.Type.*

case object Infer {
  def apply(typed: Typed, force: Boolean = false): Type = {
    infer(typed, force)
  }

  private def infer(typed: Typed, force: Boolean): Type = {
    if(!force && typed.ty != Infer) {
      typed.ty
    } else {
      infer(typed)
    }
  }

  private def infer(typed: Typed): Type = throw new RuntimeException("infer should be a type impl Typed")

  private def infer(stmt: Stmt): Type = {
    stmt match
      case Stmt.Local(name, ty, value) => {
        ty match
          case TyInfo.Spec(ty) => ???
          case TyInfo.Infer => infer(value)
          case TyInfo.Nil => Nil
      }
      case Stmt.Expr(expr) => infer(expr)
      case Stmt.While(cond, body) => infer(body)
      case Stmt.Assign(name, value) => ??? // todo:lookup name
  }

  private def infer(expr: Expr): Type = {
    expr match
      case Number(v) => Int32
      case Identifier(ident) => ???
      case Bool(b) => Boolean
      case Binary(op, lhs, rhs) => common(lhs, rhs)
      case Str(str) => String
      case If(cond, true_branch, false_branch) => false_branch match
        case Some(fBr) => common(true_branch, fBr)
        case None => infer(true_branch)
      case Return(expr) => infer(expr)
      case Block(stmts) => infer(stmts.last)
      case Call(target, args) => ???
      case Lambda(args, block) => ???
      case MethodCall(obj, target, args) => ???
      case Field(expr, ident) => ???
      case Self => ???
      case Constant(ident) => ???
      case Index(expr, i) => ???
  }

  private def common(lhs: Expr, rhs: Expr): Type = {
    val lt = infer(lhs)
    val rt = infer(rhs)
    if lt == rt then lt else Err("failed")
  }
}
