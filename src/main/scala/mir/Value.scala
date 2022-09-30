package rclang
package mir

import ty.*

class Value extends Typed {
  var uses: List[Use] = List()
  var name: String = ""
  def addUser(v: User) = {
    uses = Use(this, v)::uses
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
    operands = ops.map(Use(_, this))
    ops.foreach(op => {
      op.addUser(this)
    })
  }

  def setOperand(i: Int, v: Value) = {
    operands = operands.updated(i, Use(v, this))
    v.addUser(this)
  }

  def getOperand(i: Int) = operands(i).value
  def getOperands = operands.map(_.value)

  def replaceAllUseWith(v: Value) = {
    uses.foreach(use => {
      assert(use.value == this)
      use.value = v
    })
    uses.foreach(use => {
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
