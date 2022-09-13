package rclang
package codegen

trait TReg

class Reg(val number: Int, val length: Int = 4) extends MachineOperand() with TReg {
  override def toString: String = s"Reg$number"
}

class SpecReg(number: Int) extends Reg(number, 8)

object StackBaseReg extends SpecReg(7)

case class RetReg() extends Reg(0)

case class PCReg() extends TReg

case class ParamReg(num: Int, len: Int = 4) extends Reg(num, len)

object EAX extends Reg(0)

object RBP extends SpecReg(6)
object RSP extends SpecReg(7)
object RIP extends SpecReg(99)