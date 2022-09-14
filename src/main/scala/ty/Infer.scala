package rclang
package ty
import ast.Expr.*
import ast.*
import ty.*
import ty.TyCtxt

import scala.collection.immutable.ListMap
import rclang.mir.intrinsics
import rclang.tools.{FullName, NestSpace}

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
    if(!force && typed.ty != InferType) {
      typed.ty
    } else {
      infer(typed)
    }
  }

  private def infer(typed: Typed): Type = {
    typed match
      case expr: Expr => infer(expr)
      case item: Item => infer(item)
      case method: Method => infer(method)
      case stmt: Stmt => infer(stmt)
      case _ => ???
  }


  private def infer(item: Item): Type = {
    item match
      case m: Method => infer(m)
      case k: Class => {
        StructType(k.name.str, ListMap.from(k.vars.map(v => v.name.str -> translate(v.ty))))
      }
  }

  private def infer(stmt: Stmt): Type = {
    stmt match
      case Stmt.Local(name, ty, value) => {
        ty match
          case TyInfo.Spec(ty) => ???
          case TyInfo.Infer => infer(value)
          case TyInfo.Nil => NilType
      }
      case Stmt.Expr(expr) => infer(expr)
      case Stmt.While(cond, body) => infer(body)
      case Stmt.Assign(name, value) => lookup(name)
      case Stmt.Break() => NilType
      case Stmt.Continue() => NilType
  }

  private def infer(expr: Expr): Type = {
    expr match
      case Number(v) => Int32Type
      case Identifier(ident) => lookup(ident)
      case Bool(b) => BooleanType
      case Binary(op, lhs, rhs) => common(lhs, rhs)
      case Str(str) => StringType
      case If(cond, true_branch, false_branch) => false_branch match
        case Some(fBr) => common(true_branch, fBr)
        case None => infer(true_branch)
      case Return(expr) => infer(expr)
      case Block(stmts) => {
        if(stmts.isEmpty) {
          NilType
        } else {
          tyCtxt.enter(infer(stmts.last))
        }
      }
      case Call(target, args) => lookup(target)
      case Lambda(args, block) => ???
      case MethodCall(obj, target, args) => {
        // obj is a constant or obj is a expr
        obj match
          case Symbol(sym) => {
            NestSpace(tyCtxt.globalTable, tyCtxt.fullName.copy(klass = sym.str)).lookupFn(target).ty
          }
          case _ => {
            val ty = infer(obj)
            structTyProc(ty)(s => {
              NestSpace(tyCtxt.globalTable, tyCtxt.fullName.copy(klass = s.name)).lookupFn(target).ty
            })
          }
      }
      case Field(expr, ident) => {
        val obj = infer(expr)
        structTyProc(obj)(s => {
          NestSpace(tyCtxt.globalTable, tyCtxt.fullName.copy(klass = s.name)).lookupVar(ident).ty
        })
      }
      case Self => ???
      case Symbol(ident) => ???
      case Index(expr, i) => ???
  }

  private def lookup(ident: Ident): Type = {
    tyCtxt.lookup(ident).getOrElse(ErrType(s"$ident not found"))
  }

  private def infer(f: Method): Type = {
    tyCtxt.fullName.fn = f.name.str
    val ret = translate(f.decl.outType)
    val params = f.decl.inputs.params.map(_.ty).map(translate)
    tyCtxt.fullName.fn = ""
    FnType(ret, params)
  }

  def translate(info: TyInfo): Type = info match
    case TyInfo.Spec(ty) => translate(ty)
    case TyInfo.Infer => ErrType("can't translate TyInfo.Infer")
    case TyInfo.Nil => NilType

  def translate(ident: Ident): Type = {
    ident.str match
      case "Boolean" => BooleanType
      case "String" => StringType
      case "Int" => Int32Type
      case "Float" => FloatType
      case "Nil" => NilType
      case _ => lookup(ident)
  }

  private def common(lhs: Expr, rhs: Expr): Type = {
    val lt = infer(lhs)
    val rt = infer(rhs)
    if lt == rt then lt else ErrType("failed")
  }
}

def structTyProc[T](ty: Type)(f: StructType => T): T = {
  ty match
    case s: StructType => f(s)
    case PointerType(ty) => structTyProc(ty)(f)
    case _ => ???
}