package rclang
package codegen
import mir.*
import tools.RcLogger.*

import tools.DumpManager

import java.io.File

// todo: call c printf
// extern symbol, push arg, call
// save str
// process constant and not constant

// todo: program entry: .main
// default _start -> main
// can modify in link script


type size
class LIRBuilder {
  var regMap = Map[String, Reg]()
  var valueReg = Map[Value, Reg]()
  var basicBlock = MachineBasicBlock("base")
  var valueOperand = Map[Value, MachineOperand]()
  var strTable = Map[String, MachineOperand]()
  def bbs = List(basicBlock) // todo:fix this
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
    v match
      case c:Constant => getOrCreate(v)
      case _ => valueReg.get(v) match
        case Some(value) => value
        case None => throw new RuntimeException(s"$v not found")
  }

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
            buildLoad(AddrOfValue(RelativeReg(RIP, Offset.LabelOffset(strTable.size.toString))), strReg)
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
    basicBlock.insert(DynamicAllocInst(reg))
  }

  def buildArith(op: String, lhs: MachineOperand, rhs: MachineOperand): ArithInst = {
    basicBlock.insert(ArithInst(op, lhs, rhs))
  }

  // todo: value maybe a constant of normal value
  // when constant should be load or save in instr
  def buildLoad(v: Value): LoadInst = {
    // todo: error when not exist
    val t = getOperand(v)
    val ld = registerReg(v)
    basicBlock.insert(LoadInst(ld, t))
  }

  def buildLoad(from: MachineOperand, to: Reg): LoadInst = {
    basicBlock.insert(LoadInst(to, from))
  }

  def buildStore(v: Value, ptr: Value): StoreInst = {
    val target = getOrCreate(ptr)
    val value = v match
      case _: Constant => buildLoad(v).target
      case _ => getReg(v)
    basicBlock.insert(StoreInst(value, target))
  }

  def buildReturn(v: Value): ReturnInst = {
    val t = v match
      case _: Intrinsic => Imm(0)
      case _ => valueReg(v)
      Imm(0)
    basicBlock.insert(StoreInst(t, EAX))
    buildPop(RBP)
    basicBlock.insert(ReturnInst(t))
  }

  def buildPush(v: Value): PushInst = {
//    val value = getOrCreate(v)
    val value = getOperand(v)
    basicBlock.insert(PushInst(value))
  }

  def buildPush(v: MachineOperand): PushInst = {
    basicBlock.insert(PushInst(v))
  }

  def buildPop(target: Target): PopInst = {
    basicBlock.insert(PopInst(target))
  }

  def buildCall(target: String): CallInst = {
    // save result
    basicBlock.insert(CallInst(target))
  }

  def buildInlineASM(asm: String): InlineASM = {
    basicBlock.insert(InlineASM(asm))
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
case class CallInst(target: Label) extends MachineInst

case class InlineASM(content: String) extends MachineInst

class LIRTranslator() {
  var builder = new LIRBuilder()

  def apply(fn: Function): MachineFunction = {
    builder.buildPush(RBP)
    builder.buildLoad(RSP, RBP)
    allocInput(fn.argument)
    fn.bbs.foreach(translateBB)
    // todo: alloc local in stack
    MachineFunction(fn.name, builder.bbs, builder.valueReg, builder.strTable)
  }

  val argPassByStack = true
  // rdi, rsi, rdx, rcx, r8/r8d, r9/r9d
  def allocInput(args: List[Argument]) = {
    // esp + i
    // read from stack
    var wordLength = 4
    var offset = wordLength
    args.foreach(arg => {

      if(argPassByStack) {
        val reg = RelativeReg(StackBaseReg, Offset.NumOffset(-offset))
        // todo:fix this
        builder.valueOperand = builder.valueOperand.updated(arg, reg)
        // todo:type size
        offset += wordLength
      } else {
        // 
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
    builder.buildReturn(ret.value)
  }

  def visitCall(call: Call) = {
    // todo:save reg
    call.args.foreach(builder.buildPush(_))
    builder.registerReg(call)
    builder.buildCall(call.func.name)
    // todo: if has return, then pop ret??
  }

  def visitIntrinsic(intr: Intrinsic) = {
    val f = intr.name match
      case "open" => "op"
      case "print" => "puts@PLT"
      case _ => ???
    intr.args.foreach(builder.buildPush(_))
    builder.buildInlineASM(s"call ${f}")
  }
}

object RegisterAllocation {
  def apply(regs: List[Reg]) : List[Reg] = {
    List()
  }
}

extension (dir: String) {
  def /(file: String): String = {
    s"$dir${File.separator}$file"
  }
}

// todo:data dependence and control dependence
def toLIR(module: Module): List[MachineFunction] = {
  val fns = module.fns.map(LIRTranslator()(_)).toList
  val text = TextSection()
  val strTable = fns.flatMap(fn => (0 until fn.strTable.size).zip(fn.strTable.keys).map((i, str) => StrSection(i, List(str))))
  val rdata = RDataSection(strTable)
  println(rdata.asm)
  fns.foreach(fn => text.addFn(fn.name -> fn.instructions.map(GNUASM.toASM)))
//  text.addFn(fns(1).name -> fns(1).instructions.map(GNUASM.toASM))
  logf("asm.s", text.toString)

  as(DumpManager.getDumpRoot /"asm.s", DumpManager.getDumpRoot / "tmp.o")
  println(text)

//  val ass = Assembly()
//  println(ass.serialize)
  val legalizer = new Legalizer() {

  }
  val afterRegAlloc = RegisterAllocation(List())
  val asm = Emiter()
  fns.foreach(f => {
    println(f.name)
    println(GNUASM.toASM(f))
  })
  fns
}