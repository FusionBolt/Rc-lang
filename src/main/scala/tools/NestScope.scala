package rclang
package tools

import ast.*

import Expr.*
import ast.ImplicitConversions.*

import rclang.ty.Infer

case class FullName(var fn: MethodDecl = MethodDecl("", Params(List()), TyInfo.Nil), var klass: String = "", var module: String = Def.DefaultModule) {
  def names = List(module, klass, fn.name.str).filter(_.nonEmpty)
}

case class NestSpace(val gt: GlobalTable, val fullName: FullName) {
  def withClass(klass: String) = {
    copy(fullName = fullName.copy(klass = klass))
  }

  def localTable = {
    gt.classTable(fullName.klass).methods(fullName.fn.name)
  }

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

  // fn in SymbolTable is not be preprocessed
  def lookupFn(id: Ident, recursive: Boolean = false): Method = {
    // 1. fn, used for recursive
    if(recursive && fn.name == id) {
      fn
    } else {
      // 2. class
      val klassMethod = klassTable.allMethodsList(gt).find(_.name == id)
      val method = klassMethod.getOrElse(
        // 3. module
        module.items.find(_ match
          case m: Method => m.name == id
          case _ => false) match
            case Some(value) => value.asInstanceOf[Method]
            case None => throw new RuntimeException(s"$fullName can't find $id"))
      method.withInfer
    }
  }

  def lookupVar(id: Ident): Expr = {
    // 1. local
    localTable.locals.get(id) match
      case Some(value) => Identifier(value.name).withTy(value.ty)
      // 2. argument
      case None => {
        fn.decl.inputs.params.find(_.name == id) match
          case Some(value) => Identifier(value.name).withTy(Infer.translate(value.ty))
          case None => {
            // 3. field
            klassTable.allInstanceVars(gt).find(_.name == id) match
              case Some(value) => Field(Identifier(Def.self), id).withTy(Infer.translate(value.ty))
              case None => throw new RuntimeException()
          }
      }
  }
  def findMethodInWhichClass(id: Ident, gt: GlobalTable): Class = {
    findMethodInWhichClassImpl(klassTable, id, gt) getOrElse {
//      findMethodInWhichClassImpl(gt.classTable(Def.Kernel), id, gt)
      gt.module.items.find(_ match
        case m:Method => m.name == id
        case _ => false) match
          case Some(value) => gt.classTable(Def.Kernel).astNode
          case None => throw new RuntimeException("fn not in any class")
    }
  }

  private def findMethodInWhichClassImpl(klass: ClassEntry, id: Ident, gt: GlobalTable): Option[Class] = {
    klass.allMethods(gt).find(k => k._2.exists(_.name == id)).map(_._1)
  }
}