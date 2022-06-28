package rclang
package mir

import ty.*

trait Terminator

class Argument(var name: String, var ty: Type)

trait InBasicBlock {
  var parent: BasicBlock = null
}

trait InFunction {
  var parent: Function = null
}

sealed class Instruction extends User with InBasicBlock

case class Call(func: Function, args: List[Value]) extends Instruction with Terminator
case class CondBranch(cond: Value, true_branch: BasicBlock, false_branch: BasicBlock) extends Instruction
case class Branch(dest: BasicBlock) extends Instruction
case class Return(value: Value) extends Instruction with Terminator
case class Binary(op: String, lhs: Value, rhs: Value) extends Instruction
case class Alloc(id: String, typ: Type) extends Instruction
case class Load(ptr: Value) extends Instruction
case class Store(value: Value, ptr: Value) extends Instruction
case class PHINode(var prevs: List[(Value, BasicBlock)]) extends Instruction {
  def addIncoming(value: Value, block: BasicBlock): Unit = {
    prevs = (value, block) :: prevs
  }
}
