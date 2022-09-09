package rclang
package tools

import ast.*
import Expr.*
import ast.ImplicitConversions.*

// todo: not support nest class and module
case class FullName(var fn: String = "", var klass: String = "", var module: String = Def.DefaultModule) {
  def names = List(module, klass, fn).filter(_.nonEmpty)
}

case object FullNameMaker {
  def make(names: Seq[String]) = {
    FullName(names(0), names(1))
  }
}

case class NestSpace(val gt: GlobalTable, val fullName: FullName) {
  def withFn(fn: String) = {
    copy(fullName = fullName.copy(fn = fn))
  }

  def withClass(klass: String) = {
    copy(fullName = fullName.copy(klass = klass))
  }

  def localTable = gt.classTable(fullName.klass).methods(fullName.fn)

  def klassTable = gt.classTable(fullName.klass)

  def fn = {
    localTable.astNode
  }

  def klass = {
    klassTable.astNode
  }

  def module: RcModule = {
    RcModule(List())
  }

  // todo: if lookupFn before lower, will get fn from SymbolTable
  // fn in SymbolTable is not be preprocessed
  def lookupFn(id: Ident, recursive: Boolean = false): Method = {
    // 1. fn, used for recursive
    if(recursive && fn.name == id) {
      fn
    } else {
      // 2. class
      klassTable.allMethods(gt).find(_.name == id)
        .getOrElse(
        // 3. module
        module.items.find(_ match
          case m: Method => m.name == id
          case _ => false) match
            case Some(value) => value.asInstanceOf[Method]
            case None => throw new RuntimeException(s"$fullName can't find $id"))
    }
    // todo: lookup kernel
  }

  def lookupVar(id: Ident): Expr = {
    // 1. local
    localTable.locals.get(id) match
      case Some(value) => Identifier(value.name)
      // 2. argument
      case None => {
        fn.decl.inputs.params.find(_.name == id) match
          case Some(value) => Identifier(value.name)
          case None => {
            // 3. field
            // todo: if has this ptr
            klassTable.allInstanceVars(gt).find(_.name == id) match
              case Some(value) => Field(Identifier(Def.self), id)
              case None => throw new RuntimeException()
          }
      }
  }
}