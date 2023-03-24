package rclang
package codegen

import mir.*
import tools.In

import cats.effect.kernel.Par.instance.T

trait MapOrigin[T] {
  var origin: T = null.asInstanceOf[T]
}


type InMF = In[MachineFunction]
type InMBB = In[MachineBasicBlock]

class MachineFunction(var bbs: List[MachineBasicBlock], f: Function) extends MapOrigin[Function] {
  origin = f

  def name = f.name

  def instructions = bbs.flatMap(_.instList)
}

def bbNameTranslate(oldName: String) = s"L$oldName"

class MachineBasicBlock(var instList: List[MachineInstruction], f: MachineFunction, bb: BasicBlock) extends InMF with MapOrigin[BasicBlock] with Src {
  parent = f
  origin = bb

  def name = bbNameTranslate(bb.name)

  def insert(inst: MachineInstruction) = {
    instList = instList.appended(inst)
    inst.parent = this
  }

  def insertAtFirst(inst: MachineInstruction) = {
    instList = inst::instList
    inst.parent = this
  }
}

trait MachineOperand {
  var instParent: MachineInstruction = null.asInstanceOf[MachineInstruction]
}

trait Src extends MachineOperand

trait Dst extends MachineOperand

case class VReg(num: Int) extends Src with Dst

case class Imm(value: Int) extends Src

case class Label(name: String) extends Src

trait MachineInstruction extends InMBB with MapOrigin[Value] {
  //  dst: List[Dst], ops: List[Src], mbb: MachineBasicBlock, private val value: Value)
  //  parent = mbb
  //  origin = value
  var dstList: List[Dst]
  var ops: List[Src]

  def useIt(inst: MachineInstruction) = {
    ops.nonEmpty && inst.dstList.nonEmpty && ops.contains(inst.dstList.head)
  }

  def removeFromParent() = {
    parent.instList = parent.instList.filter(_ != this)
  }
}

case class FrameIndexInst(reg: Dst, index: Imm) extends MachineInstruction() {
  override var dstList: List[Dst] = List(reg)
  override var ops: List[Src] = List(index)
}

case class LoadInst(dst: Dst, addr: Src) extends MachineInstruction() {
  override var dstList = List(dst)
  override var ops = List(addr)
}

case class StoreInst(src: Src, addr: Src) extends MachineInstruction() {
  override var dstList: List[Dst] = List()
  override var ops: List[Src] = List(addr, src)
}

case class CallInst(targetFn: String, dst: Dst, params: List[Src]) extends MachineInstruction() {
  override var dstList = List(dst)
  override var ops = params
}

case class ReturnInst(value: Src) extends MachineInstruction() {
  override var dstList = List()
  override var ops = List(value)
}

case class BinaryInst(op: BinaryOperator, dst: Dst, lhs: Src, rhs: Src) extends MachineInstruction() {
  override var dstList = List(dst)
  override var ops = List(lhs, rhs)
}

case class CondBrInst(cond: Src, addr: Src) extends MachineInstruction {
  override var dstList = List()
  override var ops = List(cond, addr)
}

case class BranchInst(addr: Src) extends MachineInstruction {
  override var dstList = List()
  override var ops = List(addr)
}

case class PhiInst(dst: Dst, incomings: Map[Src, MachineBasicBlock]) extends MachineInstruction {
  override var dstList = List(dst)
  override var ops = List()
}

case class InlineASM(str: String) extends MachineInstruction() {
  override var dstList = List()
  override var ops = List()
}

enum BinaryOperator:
  case Add
  case Sub
  case GT
  case LT
