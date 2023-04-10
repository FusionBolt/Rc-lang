package rclang
package codegen

import mir.*

import rclang.ty.sizeof

class VRegisterManager {
  var vregMap = Map[Value, VReg]()
  var count = 0

  def createVReg(value: Value): VReg = {
    val v = VReg(count)
    vregMap = vregMap.updated(value, v)
    count += 1
    v
  }

  def getVReg(value: Value): Option[VReg] = vregMap.get(value).map(_.dup)

  def getOrCreateVReg(value: Value): VReg = getVReg(value) match
    case Some(v) => v
    case None => createVReg(value)

  def registerReg(value: Value, reg: VReg): Unit = {
    vregMap = vregMap.updated(value, reg)
  }
}

class IRTranslator {
  var vregManager = VRegisterManager()
  var currentFn: MachineFunction = null
  var builder = MachineIRBuilder()
  var localVarMap = Map[Instruction, Int]()
  var strTable = Map[String, Label]()
  var bbMap = Map[BasicBlock, MachineBasicBlock]()
  var frameInfo = MachineFrameInfo()

  private def getVReg(value: Value): Option[VReg] = vregManager.getVReg(value)

  private def getOrCreateVReg(value: Value): VReg = vregManager.getOrCreateVReg(value)

  private def createVReg(value: Value): VReg = {
    vregManager.createVReg(value)
  }

  private def registerReg(value: Value, reg: VReg): Unit = {
    vregManager.registerReg(value, reg)
  }

  def visit(fns: List[Function]): List[MachineFunction] = {
    fns.map(visit)
  }

  private def visit(fn: Function): MachineFunction = {
    frameInfo = MachineFrameInfo()
    val mf = MachineFunction(List(), fn, frameInfo)
    fn.argument.foreach(arg => {
      frameInfo.addItem(ArgItem(sizeof(arg.ty), arg))
    })
    vregManager = VRegisterManager()
    currentFn = mf
    builder = MachineIRBuilder()
    bbMap = Map[BasicBlock, MachineBasicBlock]()
    localVarMap = getLocalVarMap(fn)
    val bbs = fn.bbs.map(visitBB)
    mf.bbs = bbs
    localVarMap = null
    frameInfo = null
    mf
  }

  private def visitBB(bb: BasicBlock): MachineBasicBlock = {
    builder.mbb = MachineBasicBlock(List(), currentFn, bb)
    bbMap = bbMap.updated(bb, builder.mbb)
    bb.stmts.foreach(visitInst)
    builder.mbb
  }

  private def visitInst(i: Instruction) = {
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
      case inst: GetElementPtr => visitGetElementPtr(inst)
      //      case inst: SwitchInst => visitSwitchInst(inst)
      case inst: MultiSuccessorsInst => ??? // invalid
      case _ => println(i); ???
    inst.origin = i
    inst
  }

  def getOperand(value: Value): Src = {
    value match
      case const: Constant => getConstant(const)
      case NilValue => Imm(0)
      case Load(v) => getOperand(v)
      case Argument(id, ty) => {
        val index = currentFn.f.argument.indexWhere(_.name == id)
        frameInfo.args(index).toFrameIndex
      }
      case v: Alloc => {
        val index = findIndex(v)
        frameInfo.locals(index).toFrameIndex
      }
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

  def findIndex(alloc: Alloc): Int = localVarMap(alloc)

  def visitBinary(binary: Binary) = {
    val lhs = getOperand(binary.lhs)
    val rhs = getOperand(binary.rhs)
    builder.insertBinaryInst(BinaryOperator.valueOf(binary.op), createVReg(binary), lhs, rhs)
  }

  def visitLoad(load: Load) = {
    LoadInst(VReg(-1), VReg(-1))
//    val addr = getOperand(load.ptr)
//    builder.insertLoadInst(createVReg(load), addr)
  }

  def visitStore(store: Store) = {
    store.value match
      case ConstantArray(len, values) => {
        val addr = getOrCreateVReg(store.ptr)
        registerReg(store, addr)
        values.map(getOperand).foreach(v => {
          // todo: addr: 4, 8, 12, 16
          builder.insertStoreInst(addr, v)
        })
        builder.mbb.instList.last
      }
      case _ => {
        val src = getOperand(store.value)
        val addr = getOperand(store.ptr) match
          case v: VReg => {
            // other value use store, but reg of addr is not same as store
            registerReg(store, v)
            v
          }
          case frame: FrameIndex => frame
          case _ => ???
        builder.insertStoreInst(addr, src)
      }
  }

  def visitAlloc(alloc: Alloc) = {
//    val idx = findIndex(alloc)
    // todo: fix this
    // todo: local should in the first
    frameInfo.addItem(LocalItem(4, alloc))
    LoadInst(VReg(-1), VReg(-1))
//    builder.insertLoadInst(createVReg(alloc), FrameIndex(idx))
  }

  def visitCall(call: Call) = {
    val params = call.args.map(getOperand)
    val target = call.func.name
    val dst = createVReg(call)
    builder.insertCallInst(target, dst, params)
  }

  def visitReturn(ret: Return) = {
    // ret.value is store
    // 绑定store的addr到store上，但这是store的target构造的，而不是store本身构造的
    val value = getOperand(ret.value)
    builder.insertReturnInst(value)
  }

  def visitCondBranch(condBr: CondBranch) = {
    val cond = getOperand(condBr.cond)
    // compare reg
    builder.insrtCondBrInst(cond, Label(bbNameTranslate(condBr.trueBranch.name)))
    builder.insertBranchInst(Label(bbNameTranslate(condBr.falseBranch.name)))
  }

  def visitBranch(br: Branch) = {
    builder.insertBranchInst(Label(bbNameTranslate(br.dest.name)))
  }

  def visitPhiNode(phiNode: PhiNode) = {
    val incoming = phiNode.incomings.map((v, bb) => (getOperand(v) -> bbMap(bb))).toMap
    val dst = createVReg(phiNode)
    builder.insertPhiInst(dst, incoming)
  }

  def visitIntrinsic(inst: Intrinsic) = {
    val f = inst.name match
      case "open" => ???
      case "print" => "printf@PLT"
      case "malloc" => "malloc@PLT"
      case _ => ???
    val params = inst.args.map(getOperand)
    builder.insertCallInst(f, createVReg(inst), params)
  }

  def visitGetElementPtr(inst: GetElementPtr) = {
    val objAddr = getOperand(inst.value)
    val offset = getOperand(inst.offset)
    val expr = Binary("Add", inst.value, inst.offset)
    val fieldAddr = createVReg(expr)
    builder.insertBinaryInst(BinaryOperator.Add, fieldAddr, objAddr, offset)
    val target = createVReg(inst)
    builder.insertLoadInst(target, fieldAddr)
  }
}
