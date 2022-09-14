package rclang
package ty

import ast.Ident
import ty.Type
import tools.{FullName, GlobalTable}
import ty.Infer
import tools.NestSpace

import rclang.ty.Infer.tyCtxt

import scala.collection.immutable.Map

/**
 *
 * @param global GlobalTypeInfo
 */
case class TyCtxt() {
  var global: Map[Ident, Type] = Map()
  var globalTable: GlobalTable = null
  var fullName: FullName = FullName()
  def setGlobalTable(gt:GlobalTable) = {
    globalTable = gt
    global = globalTable.methodTypeTable.toMap.map((id, item) => id -> Infer(item))
  }

  /**
   * OuterScopes's Type only SymbolTable
   */
  var outer = List[Map[Ident, Type]]()
  /**
   * CurrentScope's Type only SymbolTable
   */
  var local = Map[Ident, Type]()

  private def getClassTy(id: Ident) = globalTable.classTable.get(id.str).map(_.astNode.infer)

  def lookup(ident: Ident): Option[Type] = {
    if (ident.str == "malloc" || ident.str == "this") {
      return Some(PointerType(getClassTy(Ident(fullName.klass)).get))
    }
    val ty = local.get(ident) orElse outer.find(_.contains(ident)).map(_(ident)) orElse global.get(ident)
    ty orElse getClassTy(ident)
  }

  /**
   * enter a block(method body or single block)
    * @param f lazy evaluated method call, before F is used, F will not be evaluated
   * @tparam T
   * @return
   */
  def enter[T](newLocal: Map[Ident, Type], fnName: String = "")(f: => T): T = {
    // (1, 2, 3) ::= 4
    // (4, 1, 2, 3)
    outer ::= local
    local = newLocal
    val oldFullName = fullName
    if(fnName.nonEmpty) {
      fullName = fullName.copy(fn = fnName)
    }
    val result = f
    local = outer.head
    outer = outer.tail
    fullName = oldFullName
    result
  }

  def enter[T](f: => T): T = {
    enter(Map())(f)
  }

  def addLocal(k: Ident, v: Type): Unit = {
    local += (k -> v)
  }
}