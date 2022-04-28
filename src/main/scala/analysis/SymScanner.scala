package rclang
package analysis

import ast.{ASTNode, ASTVisitor, Item, RcModule, Stmt, TyInfo}
import ast.Item.*
import ast.Ident
import ast.ImplicitConversions.*
import tools.{ClassEntry, GlobalTable, LocalTable, State, toState}

import scala.collection.mutable
import scala.collection.mutable.Map

/**
 * Recursively visit ast, generate info
 * 1. class list
 * 2. method local table
 * 3. global data
 */
object SymScanner extends ASTVisitor {
  // todo: scan constant
  var currentClass: State[ClassEntry] = new ClassEntry(Item.Class("Kernel", None, List(), List()))
  var currentMethod: State[LocalTable] = new LocalTable(null)
  var classTable:Map[String, ClassEntry] = Map(Def.Kernel -> currentClass.value)

  // todo:can't process nest method or class
  def apply(ast: RcModule): GlobalTable = {
    visit(ast)
    new GlobalTable(classTable)
  }

  /**
   * @param klass
   * 1. mk new ClassTable
   * 2. visit subnode and update table
   * 3. add to ClassTable
   * @return
   */
  override def visit(klass: Item.Class): R = {
    val result = currentClass.by(new ClassEntry(klass)){ () =>
      super.visit(klass)
    }
    classTable(klass.name) = result
  }

  /**
   * @param method
   * 1. mk new MethodTable
   * 2. visit subnode and update table
   * 3. add to Current ClassTable's MethodTable
   * @return
   */
  override def visit(method: Item.Method): R = {
    val result = currentMethod.by(new LocalTable(method)) { () =>
      super.visit(method)
    }
    currentClass.value.addMethod(result)
  }

  override def visit(stmt: Stmt): R = {
    stmt match {
      case local: Stmt.Local => currentMethod.value += local
      case _ =>
    }
  }
}