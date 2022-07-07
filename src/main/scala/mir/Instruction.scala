package rclang
package mir

import ty.*

trait Terminator {
  def successors: List[BasicBlock]
}

class Argument(var name: String, var ty: Type) {
  var default: Option[Value] = None
  var passByRef: Boolean = true
}

trait In[T] {
  var parent: T = null.asInstanceOf[T]
}

type InBasicBlock = In[BasicBlock]
type InFunction = In[Function]


sealed class Instruction(numOps: Int) extends User(numOps) with InBasicBlock

case class BinaryInst(lhsValue: Value, rhsValue: Value) extends Instruction(2) {
  setOperand(0, lhsValue)
  setOperand(1, rhsValue)
  def lhs: Value = getOperand(0)
  def rhs: Value = getOperand(1)
}

case class UnaryInst(operandValue: Value) extends Instruction(1) {
  setOperand(0, operandValue)
  def operand: Value = getOperand(0)
}
// todo: function is a operand?
case class Call(func: Function, args_value: List[Value]) extends Instruction(varOps) {
  setOperands(args)
  def args = getOperands
  def getArg(i: Int): Value = getOperand(i)
}

case class CondBranch(condValue: Value, tBranch: BasicBlock, fBranch: BasicBlock) extends Instruction(3) with Terminator {
  setOperand(0, condValue)
  setOperand(1, tBranch)
  setOperand(2, fBranch)
  def cond: Value = getOperand(0)
  def trueBranch: BasicBlock = getOperand(1).asInstanceOf[BasicBlock]
  def falseBranch: BasicBlock = getOperand(2).asInstanceOf[BasicBlock]
  def successors: List[BasicBlock] = List(trueBranch, falseBranch)
}

case class Branch(destBasicBlock: BasicBlock) extends Instruction(1) with Terminator {
  setOperand(0, destBasicBlock)
  def dest: BasicBlock = getOperand(0).asInstanceOf[BasicBlock]
  def successors = List(dest)
}

case class Return(value: Value) extends Instruction(1) with Terminator {
  setOperand(0, value)
  def successors = List()
}

case class Binary(op: String, lhs_value: Value, rhs_value: Value) extends Instruction(2) {
  setOperand(0, lhs_value)
  setOperand(1, rhs_value)
  def lhs = getOperand(0)
  def rhs = getOperand(1)
}

case class Alloc(var id: String, val typ: Type) extends Instruction(0)

case class Load(ptr: Value) extends Instruction(1)
case class Store(value: Value, ptr: Value) extends Instruction(2)
case class PhiNode(var incomings: Map[Value, Set[BasicBlock]] = Map()) extends Instruction(varOps) {
  // todo:fix this toString
  // avoid recursive
  private def incomingsStr = incomings.map(x => x._2.map(b => s"${x._1} => ${b.name}").mkString("\n")).mkString("\n")
  override def toString: String = "Phi"
  def addIncoming(value: Value, block: BasicBlock): Unit = {
    incomings = incomings.updated(value, incomings.getOrElse(value, Set()) + block)
  }
}

enum Constant(typ: Type) extends User(0):
  case Integer(value: Int) extends Constant(Int32Type)
  case Str(str: String) extends Constant(StringType)
  case Bool(bool: Boolean) extends Constant(BooleanType)
