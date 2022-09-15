package rclang
package mir

import ty.*

import scala.language.implicitConversions

class Value extends Typed {
  var name: String = ""
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
  var uses: List[Use] = List.fill(numOps)(Use(null, null))
  def operands = uses
  def setOperands(ops: List[Value]) = uses = ops.map(Use(_, this))
  def setOperand(i: Int, v: Value) = {
    uses =  uses.updated(i, Use(v, this))
  }
  def getOperand(i: Int) = uses(i).value
  def getOperands = uses.map(_.value)
}

object ImplicitConversions {
  implicit def useToValue(use: Use): Value = use.value

  implicit def usesToValues(uses: List[Use]): List[Value] = uses.map(_.value)
}

case class Use(var value: Value, var parent: User) {
//  override def toString: String = s"Use(${toStr(value)} => ${toStr(parent)})"
  override def toString: String = s"Use(${toStr(value)})"

//  private def toStr[T](v: T) = if v == null then "" else v.toString
  private def toStr[T](v: T) = ""
}
