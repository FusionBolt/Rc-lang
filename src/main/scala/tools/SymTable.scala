package rclang
package tools
import ast.{FieldDef, Ident, Item, MethodDecl}
import ast.Type
import ast.Stmt

import scala.collection.mutable
import scala.collection.mutable.Map

// because of id should carry position info, key' type should be String
class GlobalTable(var classTable: Map[String, ClassEntry]) {
  def classes = classTable.keys

  def kernel = classTable("Kernel")
}

case class LocalEntry(id: Int, astNode: Stmt.Local) {
  def pos = astNode.pos
  def name = astNode.name
  def ty = astNode.ty
  def initValue = astNode.value
}

class ClassEntry(val astNode: Item.Class) {
  var methods = Map.empty[String, LocalTable]
  var fields = Map.empty[String, FieldDef]

  def addField(fieldDef: FieldDef): Unit = {
    fields(fieldDef.name.str) = fieldDef
  }

  def addMethod(localTable: LocalTable): Unit = {
    methods(localTable.fnName.str) = localTable
  }
}

class LocalTable(val astNode: Item.Method) {
  var locals = Map.empty[String, LocalEntry]
  def fnName = astNode.decl.name

  def +=(local: Stmt.Local): Unit = {
    locals(local.name.str) = LocalEntry(locals.size, local)
  }
}
