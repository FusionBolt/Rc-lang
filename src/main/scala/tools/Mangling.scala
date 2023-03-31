package rclang
package tools

import rclang.ast.MethodDecl
import rclang.ty.*

// https://github.com/gchatelet/gcc_cpp_mangling_documentationotes

def manglingTypeMap(ty: Type): String = {
  ty match
    case Int32Type => "i"
    case NilType => "v"
    case PointerType(ty) => s"P${manglingTypeMap(ty)}"

}

trait Name

class ManglingFn(name: Name, params: List[Name]) {
  override def toString: String = s"_Z$name${params.mkString}"
}

case class IdentName(name: String) extends Name {
  override def toString: String = s"${name.length}$name"
}

case class ScopeName(name: String, subStr: List[Name]) extends Name {
  override def toString: String = s"N$name${subStr.mkString}E"
}

def mangling(fn: MethodDecl, outer: List[String]): String = {
  val module = outer.head
  val klass = outer(1)
  val params = fn.inputs.params match
    case p if p.isEmpty => List(IdentName(manglingTypeMap(NilType)))
    case p => p.map(param => IdentName(manglingTypeMap(Infer.translate(param.ty).value)))
  ManglingFn(
    ScopeName(module, List(IdentName(klass), IdentName(fn.name.str))),
    params
  ).toString
}