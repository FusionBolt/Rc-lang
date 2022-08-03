package rclang
package codegen

import scala.collection.immutable.ListMap
import mir.Value

class MachineBasicBlock(var nameStr: String, var stmts: List[MachineInst] = List(), var successors: List[MachineBasicBlock] = List()) {
  def insert[T <: MachineInst](inst: T): T = {
    stmts = stmts :+ inst
    inst
  }
}

class MachineFunction(var name: String, var bbs: List[MachineBasicBlock], var regMap: Map[Value, VReg]) {
  def instructions = bbs.flatMap(_.stmts)
  override def toString: String = {
    val inst = s"$name()\n${instructions.map(_.toString).mkString("\n")}"
    val reg = "RegMap\n" + regMap.map((k, v) => s"${k} -> ${v}").mkString("\n")
    inst + "\n\n" + reg
  }
}

case class ISAInst(name: String = "", fields: ListMap[String, Int] = ListMap()) {

}

case class ISA(var instSet: List[ISAInst] = List()) {
  def addInst(inst: ISAInst): ISAInst = {
    instSet = instSet :+ inst
    inst
  }
}

sealed class MachineOperand() {

}

case class VReg(name: String = "", number: Int) extends MachineOperand()

case class Reg(name: String = "", number: Int) extends MachineOperand()

case class Imm(value: Int) extends MachineOperand()