package rclang
package mir

import ty.*

trait Terminator {
  def successors: List[BasicBlock]
}

class Argument(var name: String, var ty: Type)

trait InBasicBlock {
  var parent: BasicBlock = null
}

trait InFunction {
  var parent: Function = null
}

sealed class Instruction extends User(0) with InBasicBlock

case class BinaryInst(lhsValue: Value, rhsValue: Value) extends Instruction {
  setOperand(0, lhsValue)
  setOperand(1, rhsValue)
  def lhs: Value = getOperand(0)
  def rhs: Value = getOperand(1)
}

case class UnaryInst(operandValue: Value) extends Instruction {
  setOperand(0, operandValue)
  def operand: Value = getOperand(0)
}
// todo: call number ops??
case class Call(func: Function, args: List[Value]) extends Instruction
case class CondBranch(cond: Value, true_branch: BasicBlock, false_branch: BasicBlock) extends Instruction with Terminator {
  def successors: List[BasicBlock] = List(true_branch, false_branch)
}
case class Branch(dest: BasicBlock) extends Instruction with Terminator {
  def successors = List(dest)
}
case class Return(value: Value) extends Instruction with Terminator {
  def successors = List()
}
case class Binary(op: String, lhs: Value, rhs: Value) extends Instruction
case class Alloc(id: String, typ: Type) extends Instruction
case class Load(ptr: Value) extends Instruction
case class Store(value: Value, ptr: Value) extends Instruction
case class PHINode(var prevs: Map[Value, Set[BasicBlock]] = Map()) extends Instruction {
  // todo:fix this toString
  // avoid recursive
  override def toString: String = s"Phi${prevs.values.head.map(_.name).mkString(",")}"
  def addIncoming(value: Value, block: BasicBlock): Unit = {
    prevs = prevs.updated(value, prevs.getOrElse(value, Set()) + block)
  }
}
