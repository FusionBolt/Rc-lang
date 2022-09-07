package rclang

import ast.*
import ast.ImplicitConversions.*

object Def {
  val Kernel = "Kernel"
  val self = "this"

  // return ptr type
  val newMethod = Method(MethodDecl("new", Params(List()), TyInfo.Nil), Expr.Block(List()))
  val rcObject = Class("RcObject", None, List(), List())

}
