package rclang
package tools


def manglingTypeMap(str: String) = {
  str match
    case "Int" => "i"
}

// https://github.com/gchatelet/gcc_cpp_mangling_documentationotes
case class ManglingBase(namespace: String) {
  override def toString: String = "_Z${}"
}

case class ManglingNamespace(namespace: ManglingName) {
  override def toString: String = s"N${namespace}E"
}
case class ManglingName(name: String) {
  override def toString: String = s"${name.length}$name"
}
