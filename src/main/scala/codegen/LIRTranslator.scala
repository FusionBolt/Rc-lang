package rclang
package codegen
import mir.*


class LIRBuilder {
  var regMap = Map[String, VReg]()
  var valueReg = Map[Value, VReg]()
  var basicBlock = MachineBasicBlock("base")
  def registerReg(v: Value): VReg = {
    val name = regMap.size.toString
    val vreg = VReg(name, regMap.size)
    regMap = regMap + (name -> vreg)
    valueReg = valueReg + (v -> vreg)
    vreg
  }

  def getReg(v: Value): VReg = {
    valueReg.getOrElse(v, registerReg(v))
  }

  def buildDynamicAlloc(v: Alloc, id: String): DynamicAllocInst = {
    basicBlock.insert(DynamicAllocInst())
  }

  def buildArith(op: String, lhs: MachineOperand, rhs: MachineOperand): ArithInst = {
    basicBlock.insert(ArithInst(op, lhs, rhs))
  }

  def buildLoad(v: Value): LoadInst = {
    // todo: error when not exist
    val t = getReg(v)
    val ld = registerReg(v)
    basicBlock.insert(LoadInst(ld, t))
  }

  def buildStore(target: Value, v: Value): StoreInst = {
    val t = getReg(target)
    val r = registerReg(v)
    basicBlock.insert(StoreInst(r, t))
  }

  def buildReturn(v: Value): ReturnInst = {
    val t = valueReg(v)
    basicBlock.insert(ReturnInst(t))
  }
}

sealed class MachineInst()

case class ArithInst(op: String, var lhs: MachineOperand, var rhs: MachineOperand) extends MachineInst

case class LoadInst(var target: MachineOperand, var value: MachineOperand) extends MachineInst

// todo:存到target里还是存到result里
case class StoreInst(var target: MachineOperand, var value: MachineOperand) extends MachineInst

case class DynamicAllocInst() extends MachineInst

case class ReturnInst(value: MachineOperand) extends MachineInst

object LIRTranslator {
  var builder = new LIRBuilder()

  def apply(fn: Function): MachineFunction = {
    val bbs = fn.bbs.map(translateBB)
    MachineFunction(fn.name, bbs, builder.valueReg)
  }

  def visitInst(i: Instruction): MachineInst = {
    i match
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
//      case inst: PhiNode => visitPhiNode(inst)
//      case inst: SwitchInst => visitSwitchInst(inst)
//      case inst: MultiSuccessorsInst => visitMultiSuccessorsInst(inst)
      case _ => println(i); ???
  }

  def translateBB(bb: BasicBlock) = {
    MachineBasicBlock(bb.name, bb.stmts.map(visitInst))
  }

  def visitAlloc(alloc: Alloc) = {
    builder.registerReg(alloc)
    builder.buildDynamicAlloc(alloc, alloc.id)
  }

  def visitBinary(bn: Binary) = {
    builder.registerReg(bn)
    val lhs = builder.getReg(bn.lhs)
    val rhs = builder.getReg(bn.rhs)
    builder.buildArith(bn.op, lhs, rhs)
  }

  def visitLoad(load: Load) = {
    builder.registerReg(load)
    builder.buildLoad(load.ptr)
  }

  def visitStore(store: Store) = {
    builder.registerReg(store)
    builder.buildStore(store.ptr, store.value)
  }

  def visitReturn(ret: Return) = {
    builder.buildReturn(ret.value)
  }
}

object RegisterAllocation {
  def apply(vregs: List[VReg]) : List[Reg] = {
    List()
  }
}


def toLIR(module: Module): MachineFunction = {
  val mmod = module.fns.map(LIRTranslator(_))
  val legalizer = new Legalizer() {
    
  }
  val afterRegAlloc = RegisterAllocation(List())
  val asm = Emiter()
  mmod.head
}