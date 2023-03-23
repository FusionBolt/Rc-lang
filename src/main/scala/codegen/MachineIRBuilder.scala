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

  def buildStoreInst(addr: Src, src: Src) = build(StoreInst(addr, src))

  def buildCallInst(dst: Dst, params: List[Src]) = build(CallInst(dst, params))

  def buildRetInst(src: Src) = build(ReturnInst(src))

  def buildInlineASM(str: String) = build(InlineASM(str))

  def buildBinaryInst(op: BinaryOperator, dst: Dst, lhs: Src, rhs: Src) = build(BinaryInst(op, dst, lhs, rhs))
}
