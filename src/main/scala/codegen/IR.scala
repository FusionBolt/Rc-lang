package rclang
package codegen

import codegen.MachineOperand

import scala.collection.immutable.Map
import scala.collection.immutable.ListMap
import mir.Value
import ty.Type

class MachineBasicBlock(var nameStr: String, var stmts: List[MachineInst] = List(), var successors: List[MachineBasicBlock] = List()) {
  def insert[T <: MachineInst](inst: T): T = {
    stmts = stmts :+ inst
    inst
  }
}

class MachineFunction(var name: String, var bbs: List[MachineBasicBlock], var regMap: Map[Value, Reg], var strTable: Map[String, MachineOperand] = Map()) {
  def instructions = bbs.flatMap(_.stmts)
  override def toString: String = {
    val inst = s"$name()\n${instructions.map(_.toString).mkString("\n")}"
    val reg = "RegMap\n" + regMap.map((k, v) => s"${k} -> ${v}").mkString("\n")
    inst + "\n\n" + reg + "\n"
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

class MachineOperand() {

}

case class Imm(value: Int) extends MachineOperand()

enum Offset:
  case NumOffset(int: Int)
  case LabelOffset(str: String)

case class RelativeReg(reg: Reg, offset: Offset) extends MachineOperand

case class AddrOfValue(value: MachineOperand) extends MachineOperand

case class Label(name: String) extends MachineOperand

implicit def strToLabel(str: String): Label = Label(str)

val wordLength = 4
def sizeof(operand: MachineOperand): Int = {
  operand match
    case reg1: Reg => reg1.length
    case RelativeReg(reg, offset) => reg.length
    case AddrOfValue(value) => sizeof(value)
    case Imm(value) => wordLength
    case _ => 0
}
//case class Stack() {
//  var objects = List[Reg]()
//  def getFromStack(offset: Int, ty: Type): LoadInst = {
//
//  }
//}