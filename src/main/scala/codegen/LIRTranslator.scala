package rclang
package codegen
import mir.*
import tools.RcLogger.*
import tools.DumpManager

import ty.sizeof

import java.io.File

class LIRBuilder {
  var regMap = Map[String, Reg]()
  var valueReg = Map[Value, Reg]()
  var currBasicBlock = MachineBasicBlock("base")
  var valueOperand = Map[Value, MachineOperand]()
  var strTable = Map[String, MachineOperand]()
  var bbs = List(currBasicBlock)
  def registerReg(v: Value, reg: Reg): Reg = {
    regMap = regMap + (regMap.size.toString -> reg)
    valueReg = valueReg + (v -> reg)
    reg
  }

  def registerReg(v: Value): Reg = {
    val name = regMap.size.toString
    val reg = Reg(regMap.size, sizeof(v.ty))
    regMap = regMap + (name -> reg)
    valueReg = valueReg + (v -> reg)
    reg
  }

  def getReg(v: Value): Reg = {
    v match
      case c:Constant => getOrCreate(v)
      case _ => valueReg.get(v) match
        case Some(value) => value
        case None => throw new RuntimeException(s"$v not found")
  }

  // todo:load constant value
  def getOperand(v: Value): MachineOperand = {
    // todo:bug point
    if(v.isInstanceOf[Constant]) {
      return v.asInstanceOf[Constant] match
        case Str(str) => {
          strTable.getOrElse(str, {
            // move to reg
            // from is a addr
            // todo:maybe error
            val strReg = getOrCreate(v)
            buildLoad(AddrOfValue(RelativeReg(RIP, Offset.LabelOffset(".LC" + strTable.size.toString))), strReg)
            strTable = strTable + (str -> strReg)
            strReg
          })
        }
        case _ => getOrCreate(v)
    }
    valueReg.get(v) orElse valueOperand.get(v) match
      case Some(value) => value
      case None => throw new RuntimeException(s"Unknown value $v")
  }

  def getOrCreate(v: Value): Reg = {
    valueReg.getOrElse(v, registerReg(v))
  }

  def buildDynamicAlloc(reg: Reg): DynamicAllocInst = {
    currBasicBlock.insert(DynamicAllocInst(reg))
  }

  def buildArith(op: String, lhs: MachineOperand, rhs: MachineOperand): ArithInst = {
    currBasicBlock.insert(ArithInst(op, lhs, rhs))
  }

  def buildLoad(v: Value): LoadInst = {
    val value = getOperand(v)
    val target = registerReg(v)
    currBasicBlock.insert(LoadInst(target, value))
  }

  def buildLoad(from: MachineOperand, to: Reg): LoadInst = {
    currBasicBlock.insert(LoadInst(to, from))
  }

  def buildStore(v: Value, ptr: Value): StoreInst = {
    val target = getOrCreate(ptr)
    val value = getOperand(v)
    currBasicBlock.insert(StoreInst(value, target))
  }

  def buildReturn(v: Value): ReturnInst = {
    val t = v match
      case _: Intrinsic => Imm(0)
      case _ => valueReg(v)
      Imm(0)
    currBasicBlock.insert(StoreInst(t, RetReg()))
    buildPop(RBP)
    currBasicBlock.insert(ReturnInst(t))
  }

  def buildPush(v: Value): PushInst = {
//    val value = getOrCreate(v)
    val value = getOperand(v)
    currBasicBlock.insert(PushInst(value))
  }

  def buildPush(v: MachineOperand): PushInst = {
    currBasicBlock.insert(PushInst(v))
  }

  def buildPop(target: Target): PopInst = {
    currBasicBlock.insert(PopInst(target))
  }

  def buildCall(target: String): CallInst = {
    // save result
    currBasicBlock.insert(CallInst(target))
  }

  def buildInlineASM(asm: String): InlineASM = {
    currBasicBlock.insert(InlineASM(asm))
  }
}

sealed class MachineInst()

type Target = Reg
case class ArithInst(op: String, var lhs: MachineOperand, var rhs: MachineOperand) extends MachineInst

case class LoadInst(var target: Target, var value: MachineOperand) extends MachineInst

case class StoreInst(var value: MachineOperand, var target: Target) extends MachineInst

// no action
case class DynamicAllocInst(var target: Target) extends MachineInst

case class ReturnInst(value: MachineOperand) extends MachineInst

case class PushInst(value: MachineOperand) extends MachineInst

// todo:target is a memory or reg
case class PopInst(target: Target) extends MachineInst

case class CallInst(target: MachineOperand) extends MachineInst

case class InlineASM(content: String) extends MachineInst

class LIRTranslator() {
  var builder = new LIRBuilder()

  def apply(fn: Function): MachineFunction = {
    builder.buildPush(RBP)
    builder.buildLoad(RSP, RBP)
    allocInput(fn.argument)
    fn.bbs.foreach(translateBB)
    MachineFunction(fn.name, builder.bbs, builder.valueReg, builder.strTable)
  }

  val argPassByStack = false
  def allocInput(args: List[Argument]) = {
    var wordLength = 4
    var offset = wordLength
    args.indices.zip(args).foreach((i, arg) => {

      if(argPassByStack) {
        val reg = RelativeReg(StackBaseReg, Offset.NumOffset(-offset))
        // todo:fix this
        builder.valueOperand = builder.valueOperand.updated(arg, reg)
        offset += wordLength
      } else {
        // todo:alloc value to stack
        builder.registerReg(arg, ParamReg(i, sizeof(arg.ty)))
      }
    })
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
      case inst: Intrinsic => visitIntrinsic(inst)
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
    val lhs = builder.getOperand(bn.lhs)
    val rhs = builder.getReg(bn.rhs)
    builder.registerReg(bn, rhs)
    builder.buildArith(bn.op, lhs, rhs)
  }

  def visitLoad(load: Load) = {
    val l = builder.buildLoad(load.ptr)
    builder.registerReg(load, l.target)
    l
  }

  def visitStore(store: Store) = {
    // todo:should SSA format??
    val st = builder.buildStore(store.value, store.ptr)
    builder.registerReg(store, st.target)
    st
  }

  def visitReturn(ret: Return) = {
    builder.buildReturn(ret.value)
  }

  def pushArgs(args: List[Value]) = {
    if(argPassByStack) {
      args.foreach(builder.buildPush(_))
    } else {
      args.indices.zip(args).foreach((i, arg) => {
        val op = builder.getOperand(arg)
        builder.buildLoad(op, ParamReg(i, codegen.sizeof(op)))
      })
    }
  }

  def visitCall(call: Call) = {
    // todo:save reg
    pushArgs(call.args)
    builder.registerReg(call)
    builder.buildCall(call.func.name)
  }

  def visitIntrinsic(intr: Intrinsic) = {
    val f = intr.name match
      case "open" => ???
      case "print" => "puts@PLT"
      case _ => ???
    pushArgs(intr.args)
    builder.buildInlineASM(s"call ${f}")
  }
}

object RegisterAllocation {
  def apply(regs: List[Reg]) : List[Reg] = {
    List()
  }
}

def toLIR(module: Module): List[MachineFunction] = {
  val fns = module.fns.map(LIRTranslator()(_)).toList
  fns
}