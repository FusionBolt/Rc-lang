package rclang
package codegen

import mir.*

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

  def registerReg(value: Value, reg: VReg): Unit = {
    vregMap = vregMap.updated(value, reg)
  }
}

class IRTranslator {
  val vregManager = VRegisterManager()
  val currentFn: MachineFunction = null
  val builder = MachineIRBuilder()
  var localVarMap = Map[Instruction, Int]()
  var strTable = Map[String, Label]()

  private def getVReg(value: Value): Option[VReg] = vregManager.getVReg(value)

  private def getOrCreateVReg(value: Value): VReg = vregManager.getOrCreateVReg(value)

  private def createVReg(value: Value): VReg = {
    vregManager.getVReg(value)
    vregManager.createVReg(value)
  }

  private def registerReg(value: Value, reg: VReg): Unit = {
    vregManager.registerReg(value, reg)
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
      case inst: Call => visitCall(inst)
      case inst: CondBranch => visitCondBranch(inst)
      case inst: Branch => visitBranch(inst)
      case inst: Return => visitReturn(inst)
      case inst: Binary => visitBinary(inst)
      case inst: Alloc => visitAlloc(inst)
      case inst: Load => visitLoad(inst)
      case inst: Store => visitStore(inst)
      case inst: Intrinsic => visitIntrinsic(inst)
      case inst: PhiNode => visitPhiNode(inst)
      //      case inst: SwitchInst => visitSwitchInst(inst)
      case inst: MultiSuccessorsInst => ??? // invalid
      case _ => println(i); ???
    inst.origin = i
    inst
  }

  def getOperand(value: Value): Src = {
    value match
      case const: Constant => getConstant(const)
      case _ => getOrCreateVReg(value)
  }

  def getConstant(constant: Constant): Src = {
    constant match
      case int: Integer => Imm(int.value)
      case Str(str) => {
        strTable.getOrElse(str, {
          // todo: avoid label repeat for str and bb
          strTable.getOrElse(str, {
            val label = Label(".LC" + strTable.size.toString)
            strTable = strTable + (str -> label)
            label
          })
        })
      }
      case Bool(bool) => Imm(if (bool) 1 else 0)
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
    val addr = getOperand(load.ptr)
    builder.buildLoadInst(createVReg(load), addr)
  }

  def visitStore(store: Store): MachineInstruction = {
    val src = getOperand(store.value)
    val addr = getOrCreateVReg(store.ptr)
    // other value use store, but reg of addr is not same as store
    registerReg(store, addr)
    builder.buildStoreInst(src, addr)
  }

  def visitAlloc(alloc: Alloc): MachineInstruction = {
    val idx = findIndex(alloc)
    builder.buildFrameIndexInst(createVReg(alloc), idx)
  }

  def visitCall(call: Call) = {
    val params = call.args.map(getOrCreateVReg)
    val target = call.func.name
    val dst = createVReg(call)
    builder.buildCallInst(target, dst, params)
  }

  def visitReturn(ret: Return): MachineInstruction = {
    // ret.value is store
    // 绑定store的addr到store上，但这是store的target构造的，而不是store本身构造的
    val value = getVReg(ret.value) match
      case Some(reg) => reg
      case None => ???
    builder.buildReturnInst(value)
  }

  def visitCondBranch(condBr: CondBranch) = {
    val cond = getOrCreateVReg(condBr.cond)
    val trueBr = condBr.trueBranch.name
    val falseBr = condBr.falseBranch.name
    builder.buildCondBrInst(cond, Label(trueBr), Label(falseBr))
  }

  def visitBranch(br: Branch) = {
    builder.buildBranchInst(Label(br.dest.name))
  }

  def visitPhiNode(phiNode: PhiNode) = {
    val dst = createVReg(phiNode)
    builder.buildPhiInst(dst)
  }

  def visitIntrinsic(inst: Intrinsic) = {
    val f = inst.name match
      case "open" => ???
      case "print" => "printf@PLT"
      case _ => ???
    val params = inst.args.map(getOperand)
    builder.buildCallInst(f, createVReg(inst), params)
  }
}
