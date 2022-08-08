package rclang
package codegen

trait TReg

case class Reg(number: Int) extends MachineOperand() with TReg

// todo:fix this number
object StackBaseReg extends Reg(7)

case class RetReg() extends TReg

case class PCReg() extends TReg

object EAX extends Reg(0)