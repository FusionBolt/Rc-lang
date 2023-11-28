package rclang
package analysis

import ast.*
import ast.Expr.{Block, Self}
import ast.Method
import tools.State

import scala.collection.mutable
import scala.collection.mutable.Set
import scala.language.implicitConversions

case class ValidateError(node: ASTNode, reason: String)

case class Scope(var localTable: Set[Ident] = Set()) {

  def add(ident: Ident): Boolean = {
    localTable.add(ident)
  }

  def contains(ident: Ident): Boolean = {
    localTable.contains(ident)
  }
}

case class ScopeManager() {
  private var scopes = List[Scope]()
  def enter[T](f:() => T): T = {
    enter(Params(List()), f)
  }

  def enter[T](params: Params, f:() => T): T = {
    val oldScope = scopes
    scopes ::= Scope(mutable.Set.from(params.params.map(_.name)))
    val result = f()
    scopes = oldScope
    result
  }

  def curScope: Scope = scopes.last

  def add(ident: Ident): Boolean = curScope.add(ident)

  def contains(ident: Ident): Boolean = {
    !scopes.exists(_.contains(ident))
  }

  def curContains(ident: Ident): Boolean = curScope.contains(ident)
}

type Result = List[ValidateError]

trait Validate {
  def dupNameCheck(names: List[Ident]): Result = {
    dupCheck(names, "Name")
  }

  def dupCheck[T <: ASTNode](values: List[T], valueName: String): Result = {
    val s = Set[T]()
    values.filterNot(s.add).map(n => ValidateError(n, s"$valueName $n Dup"))
  }

  def checkCond(cond: Boolean, node: ASTNode, msg: String): Result = {
    if(cond) then List() else List(ValidateError(node, msg))
  }

  def checkOption[T](v: Option[T], f: T => Result): Result = {
    v match
      case Some(v) => f(v)
      case None => List()
  }

  def valid: Result = List()
}

trait MethodValidate extends Validate {
  var scopes = ScopeManager()
  def analysis(method: Method): Result = {
    checkMethod(method)
  }

  def checkMethod(method: Method): Result = {
    checkMethodDecl(method.decl)
    checkBlock(method.body, method.decl.inputs)
  }

  def checkMethodDecl(decl: MethodDecl): Result = {
    dupCheck(decl.inputs.params.map(_.name), "MethodParam")
  }

  def checkBlock(block: Block, params: Params = Params(List())): Result = {
    scopes.enter(params, () => {
      block.stmts.flatMap(checkStmt)
    })
  }

  def checkStmt(stmt: Stmt): Result = {
    stmt match
      case Stmt.Local(name, ty, value) => checkCond(!scopes.curContains(name), name, "$name redecl in current scope")
      case Stmt.Expr(expr) => checkExpr(expr)
      case Stmt.While(cond, body) => checkExpr(cond):::checkExpr(body)
      case Stmt.Assign(name, value) => checkCond(scopes.contains(name), name, "$name not decl")
  }

  def checkExpr(expr: Expr): Result = {
    expr match
      case Expr.Identifier(id) => checkCond(scopes.contains(id), expr, "$name not decl")
      case Expr.Binary(op, lhs, rhs) => checkExpr(lhs):::checkExpr(rhs)
      case Expr.If(cond, true_branch, false_branch) => checkExpr(cond):::checkExpr(true_branch)::: {
        false_branch match
          case Some(x) => checkExpr(x)
          case None => List()
      }
      case Expr.Lambda(args, block) => checkBlock(block, args)
      case Expr.Call(target, args, _) => args.flatMap(checkExpr)
      case Expr.MethodCall(obj, target, args) => (obj::args).flatMap(checkExpr)
      case block: Block => checkExpr(block)
      case Expr.Return(expr) => checkExpr(expr)
      // Field And Call can't resolve type in this phase
      case Expr.Field(expr, ident) => checkExpr(expr)
      case Expr.Index(expr, i) => checkExpr(expr)
      case _ => valid
  }
}

trait ModuleValidate extends Validate with MethodValidate {
  def analysis(module: RcModule): Result = {
    checkModule(module)
  }

  def checkModule(module: RcModule): Result = {
    dupNameCheck(module.items.map(item => item match
      case Class(name, _, _, _, _) => name
      case Method(decl, _) => decl.name
    )):::module.items.flatMap(checkItem)
  }

  def checkItem(item: Item): Result = {
    item match
      case m: Method => checkMethod(m)
      case klass: Class => checkClass(klass)
  }

  def checkClass(klass: Class): Result = {
    klass match
      case Class(name, parent, vars, methods, _) => {
        dupNameCheck(vars.map(_.name)):::dupNameCheck(methods.map(_.decl.name))
      }
  }

  def methodsDeclValid(decls: List[MethodDecl]): Result = {
    val s = Set[(Ident, Params)]()
    decls.map(decl => (decl.name, decl.inputs)).filterNot(s.add(_)).map(t => ValidateError(t._1, s"Method ${t._1} Dup"))
  }

  def fieldDefsValid(fields: List[FieldDef]): Result = {
    fields.flatMap(fieldDefValid):::dupNameCheck(fields.map(_.name))
  }
  
  def fieldDefValid(fieldDef: FieldDef): Result = {
    fieldDef.initValue match {
      case Some(expr) => checkExpr(expr)
      case None => checkCond(fieldDef.ty != TyInfo.Infer, fieldDef, "Field without initValue need spec Type")
    }
  }
}
