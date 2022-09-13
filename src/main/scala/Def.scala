package rclang

import ast.*
import ast.ImplicitConversions.*

object Def {
  val Kernel = "Kernel"
  val DefaultModule = Kernel
  val self = "this"

  // return ptr type
  val NewMethod = Method(MethodDecl("new", Params(List()), TyInfo.Nil), Expr.Block(List()))
  val RcObject = Class("RcObject", None, List(), List())
//  def selfObj(klass: String) = Param(self, TyInfo.Spec(klass))
}
