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
  def visit(ty: Type): R = visitRecursive(ty)
  def visit(decl: MethodDecl): R = visitRecursive(decl)
  def visit(id: Id): R = {}
  def visit(param: Param): R = visitRecursive(param)
  def visit(field: FieldDef): R = visitRecursive(field)
  def visitRecursive(modules: Modules): R = modules.modules.foreach(visit)

  def visitRecursive(module: RcModule): R = {
    module.items.foreach(visit)
  }

  def visitRecursive(item: Item): R = {
    item match {
      case Item.Method(decl, block) => visit(decl); visit(block)
      case Item.Class(name, parent, vars, methods) => {
        visit(name)
        parent match {
          case Some(parentId) => visit(parentId)
          case None =>
        }
        vars.foreach(visit)
        methods.foreach(visit)
      }
      case _ => throw new RuntimeException("NoneItem")
    }
  }

  def visitRecursive(expr: Expr): R = {
    expr match {
      case Number(n) =>
      case Identifier(id) =>
      case Bool(bool) =>
      case Binary(op, lhs, rhs) =>
      case Str(s) =>
      case If(cond, true_branch, false_branch) =>
      case Lambda(args, stmts) =>
      case Call(target, args) =>
      case MethodCall(obj, target, args) =>
      case block: Block => visitRecursive(block)
      case Return(expr) =>
      case Field(obj, id) =>
      case Self =>
      case Constant(id) =>
      case Index(expr, i) =>
    }
  }

  def visitRecursive(s: Stmt): R = {
    s match {
      case Local(id, ty, value) => visit(id);visit(ty);visit(value)
      case Stmt.Expr(expr) => visit(expr)
      case While(cond, stmts) => visit(cond); visit(stmts)
      case Assign(id, value) => visit(id); visit(value)
      case _ => throw new RuntimeException("NoneStmt")
    }
  }

  def visitRecursive(value: Type): R = {

  }

  def visitRecursive(b: Block): R = {
    b.stmts.foreach(visitRecursive)
  }

  def visitRecursive(decl: MethodDecl): R = {
    visit(decl.name)
    decl.inputs.params.foreach(visit)
    visit(decl.outType)
  }

  def visitRecursive(param: Param): R = {
    visit(param.name)
    visit(param.ty)
  }

  def visitRecursive(field: FieldDef): R = {
    visit(field.name)
    visit(field.ty)
    field.initValue match {
      case Some(expr) => visit(expr)
      case _ =>
    }
  }
}