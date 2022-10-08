package rclang
package mir

import ty.*

class Value extends Typed {
  var users: List[Use] = List()
  var name: String = ""
  def addUser(v: User): Use = {
    val u = Use(this, v)
    users = u::users
    u
  }
}

class GlobalValue extends Value

class GlobalVariable extends GlobalValue {
}

case object NilValue extends GlobalValue {
  ty = NilType
}

val varOps = -1
enum NumOps:
  case Fixed(num: Int)
  case Dynamic

class User(numOps: Int) extends Value {
  var operands: List[Use] = List.fill(numOps)(Use(null, null))
  def setOperands(ops: List[Value]) = {
    operands = ops.map(_.addUser(this))
//    ops.foreach(op => {
//      op.addUser(this)
//    })
  }

  // increment users of value
  def setOperand(i: Int, v: Value) = {
    operands = operands.updated(i, v.addUser(this))
  }

  def getOperand(i: Int) = operands(i).value
  def getOperands = operands.map(_.value)

  def replaceAllUseWith(v: Value) = {
    users.foreach(use => {
      assert(use.value == this)
      use.value = v
    })
    users.foreach(use => {
      assert(use.value != this)
    })
  }
}

// todo:implicit cast, use -> value, uses -> values


case class Use(var value: Value, var parent: User) {
  //  override def toString: String = s"Use(${toStr(value)} => ${toStr(parent)})"
  override def toString: String = s"Use(${toStr(value)})"

  private def toStr[T](v: T) = if v == null then "" else v.toString
}
