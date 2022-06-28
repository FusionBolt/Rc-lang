package rclang
package mir

import ty.*

class Value extends Typed {
  var use: Use = null
}

class User() extends Value {
  var defs: List[Value] = List()
}


// todo: finish
class Use(var user: User = User())


//class Constant(typ: Type, use: Use) extends Value {
//
//}

enum Constant(typ: Type, use: Use = Use()) extends Value:
  case Integer(value: Int) extends Constant(Type.Int32)
  case Str(str: String) extends Constant(Type.String)
  case Bool(bool: Boolean) extends Constant(Type.Boolean)
