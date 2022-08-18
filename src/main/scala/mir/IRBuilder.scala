package rclang
package mir

import ast.*
import ty.*

// todo:replace with a dsl
case class IRBuilder() {
  // todo:global value number
  var idCounter = 0
  def makeId = {
    idCounter += 1
    idCounter.toString
  }
  var currentFn: Function = _
  var basicBlocks: List[BasicBlock] = List(BasicBlock(idCounter.toString))
  var currentBasicBlock: BasicBlock = basicBlocks.last

  private def insert[T <: Instruction](inst: T): T = {
    inst.parent = currentBasicBlock
    currentBasicBlock.insert(inst)
  }

  def insertBasicBlock(block: BasicBlock = createBB()): BasicBlock = {
    basicBlocks = basicBlocks :+ block
    block.parent = currentFn
    currentBasicBlock = basicBlocks.last
    basicBlocks.last
  }

  def createBB() = {
    val bb = BasicBlock(makeId)
    bb.parent = currentFn
    bb
  }
  def createPHINode() : PhiNode = insert(PhiNode())
  def createCondBr(cond: Value, True: BasicBlock, False: BasicBlock) : CondBranch = insert(CondBranch(cond, True, False))
  def createBr(dest: BasicBlock) : Branch = insert(Branch(dest))
  def createCall(func: Function, args: List[Value]) : Call = insert(Call(func, args))
  def createIntrinsic(intr: String, args: List[Value]) : Intrinsic = insert(Intrinsic(intr, args))
  def createReturn(value: Value) : Return = insert(Return(value))
  def createAlloc(name: String, typ: Type) : Alloc = insert(Alloc(name, typ))
  def createLoad(value: Value): Load = insert(Load(value))
  def createStore(value: Value, ptr: Value) : Store = insert(Store(value, ptr))
  def createBinary(op: String, lhs: Value, rhs: Value) : Binary = insert(Binary(op, lhs, rhs))
}
