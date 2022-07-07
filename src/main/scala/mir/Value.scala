package rclang
package mir

import ty.*

class Value extends Typed {
  var name: String = ""
}

class GlobalValue extends Value

class GlobalVariable extends GlobalValue {
}

val varOps = -1
class User(numOps: Int) extends Value {
  var uses: List[Use] = List.fill(numOps)(null)
  def operands = uses
  def setOperands(ops: List[Value]) = uses = ops.map(Use(_, this))
  def setOperand(i: Int, v: Value) = {
    uses = uses.updated(i, Use(v, this))
  }
  def getOperand(i: Int) = uses(i).value
  def getOperands = uses.map(_.value)
}

// todo:implicit cast, use -> value, uses -> values
case class Use(var value: Value, var parent: User)
