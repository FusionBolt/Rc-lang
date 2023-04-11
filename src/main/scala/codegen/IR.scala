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

class MachineFunction(var bbs: List[MachineBasicBlock], var f: Function, val frameInfo: MachineFrameInfo) extends MapOrigin[Function] {
  origin = f

  def name = f.name

  def instructions = bbs.flatMap(_.instList)
}

class MachineBasicBlock(var instList: List[MachineInstruction], f: MachineFunction, bb: BasicBlock, val name: String) extends InMF with MapOrigin[BasicBlock] with Src {
  instList.foreach(inst => inst.parent = this)
  parent = f
  origin = bb
  
  def insert(inst: MachineInstruction) = {
    instList = instList.appended(inst)
    inst.parent = this
    inst
  }

  def insertAtFirst(inst: MachineInstruction) = {
    instList = inst :: instList
    inst.parent = this
  }
}

trait MachineOperand {
  var instParent: MachineInstruction = null.asInstanceOf[MachineInstruction]

  def replaceFromParent(newOperand: MachineOperand) = {
    instParent.operands = instParent.operands.map(op => if op == this then newOperand else op)
    newOperand.instParent = instParent
    instParent = null
  }
}

trait Src extends MachineOperand

trait Dst extends MachineOperand

case class VReg(num: Int) extends Src with Dst {
  def dup = VReg(num)
}

case class FrameIndex(offset: Int) extends Src with Dst

case class Imm(value: Int) extends Src

case class Label(name: String) extends Src

sealed trait MachineInstruction extends InMBB with MapOrigin[Value] {
  //  dst: List[Dst], ops: List[Src], mbb: MachineBasicBlock, private val value: Value)
  //  parent = mbb
  //  origin = value
  var operands: List[MachineOperand] = List()

  def setOperand(op: MachineOperand, i: Int) = {
    operands = operands.updated(i, op)
    op.instParent = this
  }

  def getOperand[T](i: Int) = operands(i).asInstanceOf[T]

  def useIt(inst: MachineInstruction) = {
    // todo: fix this and add test
    operands.nonEmpty && inst.operands.nonEmpty && operands.contains(inst.operands.head)
  }

  def removeFromParent() = {
    parent.instList = parent.instList.filter(_ != this)
  }

  def initOperands = operands.foreach(op => op.instParent = this)
}

case class LoadInst(private var _dst: Dst, private var _addr: Src) extends MachineInstruction() {
  operands = List(_dst, _addr)
  initOperands

  def dst_=(newDst: Dst) = setOperand(newDst, 0)

  def dst: Dst = getOperand(0)

  def addr_=(newSrc: Src) = setOperand(newSrc, 1)

  def addr: Src = getOperand(1)
}

object LoadInst {
  def unapply(inst: MachineInstruction): Option[(Dst, Src)] = {
    inst match
      case l: LoadInst => Some(l.dst, l.addr)
      case _ => None
  }
}

case class StoreInst(private val _addr: Dst, private val _src: Src) extends MachineInstruction() {
  operands = List(_addr, _src)
  initOperands

  def addr_=(newDst: Dst) = setOperand(newDst, 0)

  def addr: Dst = getOperand(0)

  def src_=(newSrc: Src) = setOperand(newSrc, 1)

  def src: Src = getOperand(1)
}

object StoreInst {
  def unapply(inst: MachineInstruction): Option[(Dst, Src)] = {
    inst match
      case s: StoreInst => Some(s.addr, s.src)
      case _ => None
  }
}

case class CallInst(targetFn: String, private val _dst: Dst, private val _params: List[Src]) extends MachineInstruction() {
  operands = _params :+ _dst
  initOperands

  def paramSize = operands.size - 1

  def params: List[Src] = operands.take(paramSize).map(_.asInstanceOf[Src])

  def params_=(newParams: List[Src]) = newParams.zipWithIndex.foreach((src, i) => setOperand(src, i))

  def dst: Dst = getOperand(paramSize)

  def dst_=(newDst: Dst) = setOperand(newDst, paramSize)
}

object CallInst {
  def unapply(inst: MachineInstruction): Option[(String, Dst, List[Src])] = {
    inst match
      case c: CallInst => Some(c.targetFn, c.dst, c.params)
      case _ => None
  }
}

case class ReturnInst(private val _value: Src) extends MachineInstruction() {
  operands = List(_value)
  initOperands

  def value: Src = getOperand(0)

  def value_=(newV: Src) = setOperand(newV, 0)
}

object ReturnInst {
  def unapply(inst: MachineInstruction): Option[Src] = {
    inst match
      case r: ReturnInst => Some(r.value)
      case _ => None
  }
}

case class BinaryInst(op: BinaryOperator, private val _dst: Dst, private val _lhs: Src, private val _rhs: Src) extends MachineInstruction() {
  operands = List(_dst, _lhs, _rhs)
  initOperands

  def dst_=(newDst: Dst) = setOperand(newDst, 0)

  def dst: Dst = getOperand(0)

  def lhs_=(newLhs: Src) = setOperand(newLhs, 1)

  def lhs: Src = getOperand(1)

  def rhs_=(newRhs: Src) = setOperand(newRhs, 2)

  def rhs: Src = getOperand(2)
}

object BinaryInst {
  def unapply(inst: MachineInstruction): Option[(BinaryOperator, Dst, Src, Src)] = {
    inst match
      case b: BinaryInst => Some(b.op, b.dst, b.lhs, b.rhs)
      case _ => None
  }
}

case class CondBrInst(private val _cond: Src, private val _addr: Src) extends MachineInstruction {
  operands = List(_cond, _addr)
  initOperands

  def cond: Src = getOperand(0)

  def cond_=(newCond: Src) = setOperand(newCond, 0)

  def addr: Src = getOperand(1)

  def addr_=(newAddr: Src) = setOperand(newAddr, 1)
}

object CondBrInst {
  def unapply(inst: MachineInstruction): Option[(Src, Src)] = {
    inst match
      case c: CondBrInst => Some(c.cond, c.addr)
      case _ => None
  }
}

case class BranchInst(private val _addr: Src) extends MachineInstruction {
  operands = List(_addr)
  initOperands

  def addr: Src = getOperand(0)

  def addr_=(newAddr: Src) = setOperand(newAddr, 0)
}

object BranchInst {
  def unapply(inst: MachineInstruction): Option[(Src)] = {
    inst match
      case b: BranchInst => Some(b.addr)
      case _ => None
  }
}

case class PhiInst(private val _dst: Dst, incomings: Map[Src, MachineBasicBlock]) extends MachineInstruction {
  operands = List(_dst)
  initOperands

  def dst: Dst = getOperand(0)

  def dst_=(newDst: Dst) = setOperand(newDst, 0)
}

object PhiInst {
  def unapply(inst: MachineInstruction): Option[(Dst, Map[Src, MachineBasicBlock])] = {
    inst match
      case c: PhiInst => Some(c.dst, c.incomings)
      case _ => None
  }
}

case class InlineASM(str: String) extends MachineInstruction()

enum BinaryOperator:
  case Add
  case Sub
  case GT
  case LT
