package rclang
package mir
import ty.NilType

case class Print(arg_list: List[Value]) extends Intrinsic("print", arg_list) {
  ty = NilType
}

case class Open(arg_list: List[Value]) extends Intrinsic("open", arg_list) {
  ty = NilType
}

val intrinsics = List("print", "open")