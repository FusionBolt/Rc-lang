package rclang
package codegen

class MachineIRBuilder() {
  var mbb: MachineBasicBlock = null

  def build(inst: MachineInstruction) = {
    inst.parent = mbb
    inst
  }

  def buildFrameIndexInst(dst: Dst, index: Int) = build(FrameIndexInst(dst, Imm(index)))

  def buildLoadInst(dst: Dst, addr: Src) = build(LoadInst(dst, addr))

  def buildStoreInst(src: Src, addr: Src) = build(StoreInst(src, addr))

  def buildCallInst(targetFn: String, dst: Dst, params: List[Src]) = build(CallInst(targetFn, dst, params))

  def buildReturnInst(src: Src) = build(ReturnInst(src))

  def buildBinaryInst(op: BinaryOperator, dst: Dst, lhs: Src, rhs: Src) = build(BinaryInst(op, dst, lhs, rhs))

  def buildBranchInst(addr: Src) = build(BranchInst(addr))

  def buildCondBrInst(cond: Src, trueAddr: Src, falseAddr: Src) = build(CondBrInst(cond, trueAddr, falseAddr))

  def buildPhiInst(dst: Dst) = build(PhiInst(dst))

  def buildInlineASM(str: String) = build(InlineASM(str))
}
