package rclang
package codegen

import mir.{Alloc, BasicBlock, Binary, Bool, Constant, Function, Instruction, Integer, Load, Return, Store, Str, Value}

class VRegisterManager {
  var vregMap = Map[Value, VReg]()
  var count = 0

  def createVReg(value: Value): VReg = {
    val v = VReg(count)
    vregMap = vregMap.updated(value, v)
    count += 1
    v
  }

  def getVReg(value: Value): Option[VReg] = vregMap.get(value)

  def getOrCreateVReg(value: Value): VReg = vregMap.getOrElse(value, createVReg(value))
}

class IRTranslator {
  val vregManager = VRegisterManager()
  val currentFn: MachineFunction = null
  val builder = MachineIRBuilder()
  var localVarMap = Map[Instruction, Int]()

  private def getVReg(value: Value): Option[VReg] = vregManager.getVReg(value)

  private def getOrCreateVReg(value: Value): VReg = vregManager.getOrCreateVReg(value)

  private def createVReg(value: Value): VReg = {
    vregManager.getVReg(value)
    vregManager.createVReg(value)
  }

  def visit(fn: Function): MachineFunction = {
    localVarMap = getLocalVarMap(fn)
    val bbs = fn.bbs.map(visitBB)
    localVarMap = null
    MachineFunction(bbs, fn)
  }

  def visitBB(bb: BasicBlock): MachineBasicBlock = {
    builder.mbb = MachineBasicBlock(List(), currentFn, bb)
    val instList = bb.stmts.map(visitInst)
    builder.mbb.instList = instList
    builder.mbb
  }

  def visitInst(i: Instruction): MachineInstruction = {
    val inst = i match
      //      case inst: BinaryInstBase => visitBinaryInstBase(inst)
      //      case inst: UnaryInst => visitUnaryInst(inst)
      //      case inst: Call => visitCall(inst)
      //      case inst: CondBranch => visitCondBranch(inst)
      //      case inst: Branch => visitBranch(inst)
      case inst: Return => visitReturn(inst)
      case inst: Binary => visitBinary(inst)
      case inst: Alloc => visitAlloc(inst)
      case inst: Load => visitLoad(inst)
      case inst: Store => visitStore(inst)
      //      case inst: Intrinsic => visitIntrinsic(inst)
      //      case inst: PhiNode => visitPhiNode(inst)
      //      case inst: SwitchInst => visitSwitchInst(inst)
      //      case inst: MultiSuccessorsInst => visitMultiSuccessorsInst(inst)
      case _ => println(i); ???
    inst.origin = i
    inst
  }

  def getOperand(value: Value): Src = {
    value match
      case const: Constant => getConstant(const)
      case _ => getOrCreateVReg(value)
  }

  def getConstant(constant: Constant) = {
    constant match
      case int: Integer => Imm(int.value)
      case Str(str) => ???
      case Bool(bool) => ???
      case _ => ???
  }

  def getLocalVarMap(fn: Function) = {
    fn.instructions.filter(inst => inst.isInstanceOf[Alloc]).zipWithIndex.toMap
  }

  def findIndex(alloc: Alloc): Int = {
    localVarMap(alloc)
  }

  def visitBinary(binary: Binary): MachineInstruction = {
    val lhs = getOperand(binary.lhs)
    val rhs = getOperand(binary.rhs)
    builder.buildBinaryInst(BinaryOperator.Add, createVReg(binary), lhs, rhs)
  }

  def visitLoad(load: Load): MachineInstruction = {
    val addr = getVReg(load.ptr) match
      case Some(value) => value
      case None => ???
    builder.buildLoadInst(createVReg(load), addr)
  }

  def visitStore(store: Store): MachineInstruction = {
    val src = getOperand(store.value)
    val addr = getOrCreateVReg(store.ptr)
    builder.buildStoreInst(addr, src)
  }

  def visitAlloc(alloc: Alloc): MachineInstruction = {
    val idx = findIndex(alloc)
    builder.buildFrameIndexInst(createVReg(alloc), idx)
  }

  def visitReturn(ret: Return): MachineInstruction = {
    val value = getOrCreateVReg(ret.value)
    builder.buildRetInst(value)
  }
}
