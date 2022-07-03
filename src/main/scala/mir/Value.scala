package rclang
package mir

import ty.*

class Value extends Typed {
  var use = List[Use]()
}

class User(numOps: Int) extends Value {
  // todo:list numOps
  var operands: List[Value] = List()
  def setOperand(i: Int, op: Value) = {
    operands = operands.updated(i, op)
    op.use.appended(Use(this))
    // todo:appended??
  }
  def getOperand(i: Int) = operands(i)
}

// todo: finish
class Use(var value: Value = null)

enum Constant(typ: Type, use: Use = Use()) extends Value:
  case Integer(value: Int) extends Constant(Type.Int32)
  case Str(str: String) extends Constant(Type.String)
  case Bool(bool: Boolean) extends Constant(Type.Boolean)
