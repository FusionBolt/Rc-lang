package rclang
package tools
import ast.{FieldDef, Ident, Item, MethodDecl}
import ast.TyInfo
import ast.Stmt
import ast.ImplicitConversions.*
import ast.*

import scala.collection.mutable
import scala.collection.mutable.Map

// because of id should carry position info, key' type should be String
class GlobalTable(var classTable: Map[String, ClassEntry]) {
  def classes = classTable.keys

  def kernel = classTable(Def.Kernel)

  def methodTypeTable: Map[Ident, Item] =
    kernel.methods.map((name, local) => (local.astNode.decl.name -> local.astNode.asInstanceOf[Item]))

  def apply(id: String): Item = {
    kernel.methods(id).astNode
  }
}

case class LocalEntry(id: Int, astNode: Stmt.Local) {
  def pos = astNode.pos
  def name = astNode.name
  def ty = astNode.ty
  def initValue = astNode.value
}

class ClassEntry(val astNode: Class) {
  var methods = Map.empty[String, LocalTable]
//  var fields = Map.empty[String, FieldDef]

  def fields = astNode.vars.map(v => (v.name -> v)).toMap
//  def addField(fieldDef: FieldDef): Unit = {
//    fields(fieldDef.name.str) = fieldDef
//  }

  def addMethod(localTable: LocalTable): Unit = {
    methods(localTable.fnName.str) = localTable
  }

  def allMethods(gt: GlobalTable): List[Method] = {
    val parentMethods = astNode.parent match
      case Some(parent) => gt.classTable(parent).allMethods(gt)
      case None => List()
    astNode.methods:::parentMethods
  }

  def allInstanceVars(gt: GlobalTable): List[FieldDef] = {
    val parentVars = astNode.parent match
      case Some(parent) => gt.classTable(parent).allInstanceVars(gt)
      case None => List()
    astNode.vars:::parentVars
  }
}

class LocalTable(val astNode: Method) {
  var locals = Map.empty[String, LocalEntry]
  def fnName = astNode.decl.name

  def +=(local: Stmt.Local): Unit = {
    locals(local.name.str) = LocalEntry(locals.size, local)
  }
}
