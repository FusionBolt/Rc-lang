package rclang
package codegen
import mir.*


class LIRBuilder {
  var regMap = Map[String, Reg]()
  var valueReg = Map[Value, Reg]()
  var basicBlock = MachineBasicBlock("base")
  def registerReg(v: Value, reg: Reg): Reg = {
    regMap = regMap + (regMap.size.toString -> reg)
    valueReg = valueReg + (v -> reg)
    reg
  }

  def registerReg(v: Value): Reg = {
    val name = regMap.size.toString
    val reg = Reg(regMap.size)
    regMap = regMap + (name -> reg)
    valueReg = valueReg + (v -> reg)
    reg
  }

  def getReg(v: Value): Reg = {
    valueReg.getOrElse(v, registerReg(v))
  }

  def buildDynamicAlloc(reg: Reg): DynamicAllocInst = {
    basicBlock.insert(DynamicAllocInst(reg))
  }

  def buildArith(op: String, lhs: MachineOperand, rhs: MachineOperand): ArithInst = {
    basicBlock.insert(ArithInst(op, lhs, rhs))
  }

  // todo: value maybe a constant of normal value
  // when constant should be load or save in instr
  def buildLoad(v: Value): LoadInst = {
    // todo: error when not exist
    val t = getReg(v)
    val ld = registerReg(v)
    basicBlock.insert(LoadInst(ld, t))
  }

  def buildStore(v: Value, ptr: Value): StoreInst = {
    val target = getReg(ptr)
    val value = v match
      case _: Constant => buildLoad(v).target
      case _ => getReg(v)
    basicBlock.insert(StoreInst(value, target))
  }

  def buildReturn(v: Value): ReturnInst = {
    val t = valueReg(v)
    basicBlock.insert(ReturnInst(t))
  }

  def buildPush(v: Value): PushInst = {
    val value = getReg(v)
    basicBlock.insert(PushInst(value))
  }

  def buildPop(target: Target): PopInst = {
    basicBlock.insert(PopInst(target))
  }

  def buildCall(target: String): CallInst = {
    basicBlock.insert(CallInst(target))
  }
}

sealed class MachineInst()

type Target = Reg
case class ArithInst(op: String, var lhs: MachineOperand, var rhs: MachineOperand) extends MachineInst

case class LoadInst(var target: Target, var value: MachineOperand) extends MachineInst

// todo:存到target里还是存到result里
case class StoreInst(var value: MachineOperand, var target: Target) extends MachineInst

case class DynamicAllocInst(var target: Target) extends MachineInst

case class ReturnInst(value: MachineOperand) extends MachineInst

case class PushInst(value: MachineOperand) extends MachineInst

// todo:target is a memory or reg
case class PopInst(target: Target) extends MachineInst

// todo: target is a addr
case class CallInst(target: String) extends MachineInst

class LIRTranslator() {
  var builder = new LIRBuilder()

  def apply(fn: Function): MachineFunction = {
    val bbs = fn.bbs.map(translateBB)
    // todo: alloc all
    MachineFunction(fn.name, bbs, builder.valueReg)
  }

  def allocInput(args: List[Argument]) = {
    // esp + i
    // read from stack
  }

  def visitInst(i: Instruction): MachineInst = {
    i match
//      case inst: BinaryInstBase => visitBinaryInstBase(inst)
//      case inst: UnaryInst => visitUnaryInst(inst)
      case inst: Call => visitCall(inst)
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
    val r = builder.registerReg(alloc)
    builder.buildDynamicAlloc(r)
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
    // todo:should SSA format??
    val st = builder.buildStore(store.value, store.ptr)
    builder.registerReg(store, st.target)
    st
  }

  def visitReturn(ret: Return) = {
    // todo:store to eax
    builder.buildReturn(ret.value)
  }

  def visitCall(call: Call) = {
    // todo:save reg
    call.args.foreach(builder.buildPush(_))
    builder.buildCall(call.func.name)
    // todo: if has return, then pop ret??
  }
}

object RegisterAllocation {
  def apply(regs: List[Reg]) : List[Reg] = {
    List()
  }
}

// todo:data dependence and control dependence
def toLIR(module: Module): List[MachineFunction] = {
  val fns = module.fns.map(LIRTranslator()(_)).toList
  val text = TextSection()
  fns.foreach(fn => text.addFn(fn.name -> fn.instructions.map(_.toString)))
  println(text)
//  val ass = Assembly()
//  println(ass.serialize)
  val legalizer = new Legalizer() {

  }
  val afterRegAlloc = RegisterAllocation(List())
  val asm = Emiter()
  fns
}