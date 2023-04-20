package rclang
package tools
import ast.{FieldDef, Ident, Item, MethodDecl}
import ast.TyInfo
import ast.Stmt
import ast.ImplicitConversions.*
import ast.*

import rclang.ty.{Infer, Type}

import scala.collection.mutable
import scala.collection.mutable.Map

object GlobalTablePrinter {
  def print(globalTable: GlobalTable): Unit = {
    println("GlobalTable")
    globalTable.classTable.foreach( (klassName, klass) => {
      println(klassName)
      print(klass)
    })
    println("GlobalTable End")
  }

  def print(classEntry: ClassEntry): Unit = {
    classEntry.methods.foreach((_, method) => {
      println(method.fnName)
      println(method.locals)
    })
  }

  def print(localTable: LocalTable): Unit = {
    println(localTable.locals)
  }
}
// because of id should carry position info, key' type should be String
class GlobalTable(var classTable: Map[String, ClassEntry], var module: RcModule) {
  classTable.values.foreach(klass => {
    klass.gt = this
  })
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
  var gt: GlobalTable = null
  var methods = Map.empty[String, LocalTable]
//  var fields = Map.empty[String, FieldDef]

  def lookupMethods(name: String, gt: GlobalTable): Method = {
    methods.get(name) match
      case Some(value) => value.astNode
      case None => {
        val parentName = astNode.parent match
          case Some(value) => value
          case None => ???
        gt.classTable(parentName).lookupMethods(name, gt)
      }
  }

  def lookupFieldTy(field: Ident): Type = {
    allInstanceVars(gt).find(_.name == field) match
      case Some(value) => {
        value.ty match
          case TyInfo.Spec(ty) => Infer.translate(ty)
          case TyInfo.Infer => value.initValue.get.infer
          case TyInfo.Nil => ???
      }
      case None => ???
  }

  def fields = astNode.vars.map(v => (v.name -> v)).toMap
//  def addField(fieldDef: FieldDef): Unit = {
//    fields(fieldDef.name.str) = fieldDef
//  }

  def addMethod(localTable: LocalTable): Unit = {
    methods(localTable.fnName.str) = localTable
  }

  private def allMethodsImpl(gt: GlobalTable): Map[Class, List[Method]] = {
    val parentMethods = astNode.parent match
      case Some(parent) => gt.classTable(parent).allMethods(gt)
      case None => Map()
    Map(astNode -> astNode.methods) ++ parentMethods
  }

  def allMethods(gt: GlobalTable): Map[Class, List[Method]] = {
    allMethodsImpl(gt) ++ Map()
  }

  def allMethodsList(gt: GlobalTable): List[Method] = {
    val parentMethods = astNode.parent match
      case Some(parent) => gt.classTable(parent).allMethodsList(gt)
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
