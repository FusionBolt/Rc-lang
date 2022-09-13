package rclang
package mir
import ty.NilType

case class Print(arg_list: List[Value]) extends Intrinsic("print", arg_list) {
  ty = NilType
}

case class Open(arg_list: List[Value]) extends Intrinsic("open", arg_list) {
  ty = NilType
}

case class Malloc(arg_list: List[Value]) extends Intrinsic("malloc", arg_list) {
  // todo: error type, check arg size == 1, only size
  ty = NilType
}

// todo: simplify this
val intrinsics = List("print", "open", "malloc")