package rclang
package ty

import ast.Ident
import ty.Type
import tools.GlobalTable
import ty.Infer

import rclang.mir.NestSpace

import scala.collection.immutable.Map

/**
 *
 * @param global GlobalTypeInfo
 */
case class TyCtxt() {
  // todo: this should be val? and fix TyCtxtTest
  var global: Map[Ident, Type] = Map()
  var globalTable: GlobalTable = null
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

  def lookup(ident: Ident): Option[Type] = {
    val ty = local.get(ident) orElse outer.find(_.contains(ident)).map(_(ident)) orElse global.get(ident)
    ty
  }

  /**
   * enter a block(method body or single block)
    * @param f lazy evaluated method call, before F is used, F will not be evaluated
   * @tparam T
   * @return
   */
  def enter[T](f: => T): T = {
    // (1, 2, 3) ::= 4
    // (4, 1, 2, 3)
    outer ::= local
    local = Map[Ident, Type]()
    val result = f
    local = outer.head
    outer = outer.tail
    result
  }

  def addLocal(k: Ident, v: Type): Unit = {
    local += (k -> v)
  }
}