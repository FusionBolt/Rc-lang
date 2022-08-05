package rclang
package codegen

trait TReg

case class Reg(number: Int) extends MachineOperand() with TReg

case class StackReg() extends TReg

case class RetReg() extends TReg

case class PCReg() extends TReg