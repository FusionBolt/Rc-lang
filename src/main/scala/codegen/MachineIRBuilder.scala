package rclang
package codegen

class MachineIRBuilder() {
  var mbb: MachineBasicBlock = null

  def insert(inst: MachineInstruction) = {
    mbb.insert(inst)
  }
  
  def insertLoadInst(dst: Dst, addr: Src) = insert(LoadInst(dst, addr))

  def insertStoreInst(src: Src, addr: Src) = insert(StoreInst(src, addr))

  def insertCallInst(targetFn: String, dst: Dst, params: List[Src]) = insert(CallInst(targetFn, dst, params))

  def insertReturnInst(src: Src) = insert(ReturnInst(src))

  def insertBinaryInst(op: BinaryOperator, dst: Dst, lhs: Src, rhs: Src) = insert(BinaryInst(op, dst, lhs, rhs))

  def insertBranchInst(addr: Src) = insert(BranchInst(addr))

  def insrtCondBrInst(cond: Src, addr: Src) = insert(CondBrInst(cond, addr))

  def insertPhiInst(dst: Dst, incoming: Map[Src, MachineBasicBlock]) = insert(PhiInst(dst, incoming))

  def insrtInlineASM(str: String) = insert(InlineASM(str))
}
