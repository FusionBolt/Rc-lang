package rclang
package mir
import ty.{NilType, PointerType}
import tools.Debugger.*;

case class Print(arg_list: List[Value]) extends Intrinsic("print", arg_list) {
  ty = NilType
}

case class Open(arg_list: List[Value]) extends Intrinsic("open", arg_list) {
  ty = NilType
}

case class Malloc(arg_list: List[Value]) extends Intrinsic("malloc", arg_list) {
  check(arg_list.size == 1, s"malloc arg size should be 1, but get ${arg_list.size}")
  ty = PointerType(NilType)
}

val intrinsics = List("print", "open", "malloc")