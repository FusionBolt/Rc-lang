package rclang
package ast
import ast.{ASTNode, Modules}
import ast.Expr.*
import Stmt.{Local, While, Assign}

trait ASTVisitor {
  type R = Unit

  def visit(modules: Modules): R = visitRecursive(modules)

  def visit(module: RcModule): R = visitRecursive(module)

  def visit(item: Item): R = visitRecursive(item)

  def visit(expr: Expr): R = visitRecursive(expr)

  def visit(stmt: Stmt): R = visitRecursive(stmt)

  def visit(ty: TyInfo): R = visitRecursive(ty)

  def visit(decl: MethodDecl): R = visitRecursive(decl)

  def visit(ident: Ident): R = {}

  def visit(param: Param): R = visitRecursive(param)

  def visit(field: FieldDef): R = visitRecursive(field)

  def visit(method: Method): R = visitRecursive(method)

  def visit(klass: Class): R = visitRecursive(klass)

  final def visitRecursive(modules: Modules): R = modules.modules.foreach(visit)

  final def visitRecursive(module: RcModule): R = {
    module.items.foreach(visit)
  }

  final def visitRecursive(item: Item): R = {
    item match {
      case method: Method => visit(method)
      case klass: Class => visit(klass)
      case _ => throw new RuntimeException("NoneItem")
    }
  }

  final def visitRecursive(expr: Expr): R = {
    expr match {
      case Number(n) =>
      case Identifier(id) =>
      case Bool(bool) =>
      case Binary(op, lhs, rhs) =>
      case Str(s) =>
      case If(cond, true_branch, false_branch) =>
      case Lambda(args, stmts) =>
      case Call(target, args, _) =>
      case MethodCall(obj, target, args) =>
      case block: Block => visitRecursive(block)
      case Return(expr) =>
      case Field(obj, id) =>
      case Self =>
      case Symbol(id, _) =>
      case Index(expr, i) =>
    }
  }

  final def visitRecursive(s: Stmt): R = {
    s match {
      case Local(id, ty, value) => visit(id); visit(ty); visit(value)
      case Stmt.Expr(expr) => visit(expr)
      case While(cond, stmts) => visit(cond); visit(stmts)
      case Assign(id, value) => visit(id); visit(value)
      case _ => throw new RuntimeException("NoneStmt")
    }
  }

  final def visitRecursive(value: TyInfo): R = {

  }

  final def visitRecursive(b: Block): R = {
    b.stmts.foreach(visit)
  }

  final def visitRecursive(decl: MethodDecl): R = {
    visit(decl.name)
    decl.inputs.params.foreach(visit)
    visit(decl.outType)
  }

  final def visitRecursive(param: Param): R = {
    visit(param.name)
    visit(param.ty)
  }

  final def visitRecursive(field: FieldDef): R = {
    visit(field.name)
    visit(field.ty)
    field.initValue match {
      case Some(expr) => visit(expr)
      case _ =>
    }
  }

  final def visitRecursive(method: Method): R = {
    visit(method.decl)
    visit(method.body)
  }

  final def visitRecursive(klass: Class): R = {
    visit(klass.name)
    klass.parent match {
      case Some(parent) => visit(parent)
      case None =>
    }
    klass.vars.foreach(visit)
    klass.methods.foreach(visit)
  }
}