package rclang
package ty
import ast.Expr.*
import ast.*
import ty.Type.*
import ty.TyCtxt

case object Infer {
  var tyCtxt: TyCtxt = TyCtxt()
  def enter[T](tyCtxt: TyCtxt, f: => T): T = {
    this.tyCtxt = tyCtxt
    tyCtxt.enter(f)
  }

  def enter[T](f: => T): T = {
    tyCtxt.enter(f)
  }

  def apply(typed: Typed, force: Boolean = false): Type = {
    infer(typed, force)
  }

  private def infer(typed: Typed, force: Boolean): Type = {
    if(!force && typed.ty != Type.Infer) {
      typed.ty
    } else {
      infer(typed)
    }
  }

  private def infer(typed: Typed): Type = {
    typed match
      case expr: Expr => infer(expr)
      case item: Item => infer(item)
      case method: Item.Method => infer(method)
      case stmt: Stmt => infer(stmt)
      case _ => ???
  }


  private def infer(item: Item): Type = {
    item match
      case m: Item.Method => infer(m)
      case _ => ???
  }

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
      case Stmt.Assign(name, value) => lookup(name)
  }

  private def infer(expr: Expr): Type = {
    expr match
      case Number(v) => Int32
      case Identifier(ident) => lookup(ident)
      case Bool(b) => Boolean
      case Binary(op, lhs, rhs) => common(lhs, rhs)
      case Str(str) => String
      case If(cond, true_branch, false_branch) => false_branch match
        case Some(fBr) => common(true_branch, fBr)
        case None => infer(true_branch)
      case Return(expr) => infer(expr)
      case Block(stmts) => tyCtxt.enter(infer(stmts.last)) // todo: maybe early return
      case Call(target, args) => lookup(target)
      case Lambda(args, block) => ???
      case MethodCall(obj, target, args) => ???
      case Field(expr, ident) => ???
      case Self => ???
      case Constant(ident) => ???
      case Index(expr, i) => ???
  }

  private def lookup(ident: Ident): Type = {
    tyCtxt.lookup(ident).getOrElse(Err(s"$ident not found"))
  }

  private def infer(f: Item.Method): Type = {
    val ret = translate(f.decl.outType)
    val params = f.decl.inputs.params.map(_.ty).map(translate)
    Fn(ret, params)
  }

  def translate(info: TyInfo): Type = info match
    case TyInfo.Spec(ty) => ???
    case TyInfo.Infer => Err("can't translate TyInfo.Infer")
    case TyInfo.Nil => Nil

//  private def translate(ident: Ident): Type = {
//    // todo:other good way?
//    List("Boolean", "String", "Int32", "Float").find(_.str == ident) match
//      case Some(value) => Type.valueOf(value)
//      case None => ??? // todo:find in ctxt
//  }

  private def common(lhs: Expr, rhs: Expr): Type = {
    val lt = infer(lhs)
    val rt = infer(rhs)
    if lt == rt then lt else Err("failed")
  }
}
