package rclang
package codegen

import codegen.CallingConvention.x86_64

class GeneralX64Machine extends TargetMachine {
  val cpu = "general"
  val callingConvention = x86_64
  val wordSize = if callingConvention == x86_64 then 8 else 4
  val gregCount = 16
  val regInfos = List()
  val asmEmiter = GNUASMEmiter()
}

class GNUASMEmiter extends ASMEmiter {
  override def emitMF(mf: MachineFunction): MFText = {
    // impl convert
    val label = ASMLabel(s"${mf.name}:")
    val saveRBP = ASMInstr("pushq %rbp")
    val setRBP = ASMInstr("movq %rsp, %rbp")
    val allocStackFrame = ASMInstr("subq $" + s"${mf.frameInfo.alignLength}, %rsp")
    val initArg = mf.frameInfo.args.zipWithIndex.map((item, i) => {
      ASMInstr(s"${instStr("mov", item.len)} ${paramReg(i, item.len)}, ${-item.offset}(%rbp)")
    })
    val asm = List(label, saveRBP, setRBP, allocStackFrame) ::: initArg ::: mf.bbs.flatMap(emitMBB)
    MFText(asm, mf)
  }

  override def emitMBB(mbb: MachineBasicBlock): List[ASMText] = {
    List(ASMLabel(s".${mbb.name}:")) ::: mbb.instList.flatMap(emitInstr)
  }

  override def emitInstr(instr: MachineInstruction): List[ASMText] = {
    instr match
      case BinaryInst(op, dst, lhs, rhs) => binaryInstToASM(op.toString, dst, lhs, rhs)
      // load store should not same in mem
      case LoadInst(target, value) => value match
        case Label(label) => List(s"leaq $label(%rip), ${operandToASM(target)}")
        case _ => List(s"${instStr("mov", value)} ${operandToASM(value)}, ${operandToASM(target)}")
      case StoreInst(target, value) => {
        if ((target.isInstanceOf[FrameIndex] && value.isInstanceOf[FrameIndex]) || value.isInstanceOf[MemoryOperand] || target.isInstanceOf[MemoryOperand]) {
          val tmpReg = numToReg4(0)
          val movToTmp = s"${instStr("mov", value)} ${operandToASM(value)}, $tmpReg"
          val store = s"${instStr("mov", target)} $tmpReg, ${operandToASM(target)}"
          List(movToTmp, store)
        } else {
          List(s"${instStr("mov", target)} ${operandToASM(value)}, ${operandToASM(target)}")
        }

      }
      // todo: value size
      //      case ReturnInst(value) => List(s"${instStr("mov", value)} ${operandToASM(value)}, %eax", "popq %rbp", "ret")
      case ReturnInst(value) => List(s"${instStr("mov", value)} ${operandToASM(value)}, %eax", "leave", "ret")
      case CallInst(target, dst, args) => {
        val argList = args.zipWithIndex.map((value, i) => value match
          case Label(label) => s"leaq ${label}(%rip), ${paramReg(i, 8)}"
          case _ => s"${instStr("mov", value)} ${operandToASM(value)}, ${paramReg(i, 4)}").reverse
        val call = s"call $target"
        val dstLen = 4
        val saveResult = s"${instStr("mov", dst)} ${getRegASM(dstLen, 0)}, ${operandToASM(dst)}"
        (argList ::: List(call, saveResult)).map(ASMInstr)
      }
      case InlineASM(content) => List(content)
      case BranchInst(label) => List(s"jmp .${operandToASM(label)}")
      case CondBrInst(cond, addr) => List(s"jne .${operandToASM(addr)}")
      case PhiInst(dst, _) => throw new Exception()
      case x => println(x.getClass.toString); ???
  }

  def operandToASM(operand: MachineOperand, immWithPrefix: Boolean = true): String = {
    operand match
      case Imm(value) => {
        val prefix = if immWithPrefix then "$" else ""
        prefix + value.toString
      }
      case r: VReg => regToASM(r)
      case Label(name) => name
      case FrameIndex(index) => s"${-index}(%rbp)"
      case MemoryOperand(base, dis, index, scale) => {
        if(index.isDefined || scale.isDefined || dis.isEmpty) {
          ???
        }
        base match
          case FrameIndex(frameIndex) => s"${-(frameIndex + dis.get.value)}(%rbp)"
          case _ => ???
      }

      case _ => ???
  }

  def binaryInstToASM(op: String, dst: MachineOperand, lhs: MachineOperand, rhs: MachineOperand): List[ASMText] = {
    def toStr(op: String): List[ASMText] = {
      val lhsSize = 4
      val eax = getRegASM(lhsSize, 0) // eax == 0
      val ebx = getRegASM(lhsSize, 1) // eax == 0
      val lhsMov = s"${instStr("mov", lhs)} ${operandToASM(lhs)}, $ebx" // eax = lhs
      val rhsMov = s"${instStr("mov", rhs)} ${operandToASM(rhs)}, $eax" // eax = lhs
      val bn = s"${instStr(op, lhs)} $ebx, $eax" // eax *= rhs
      val mv = s"${instStr("mov", dst)} $eax, ${operandToASM(dst)}" // dst = eax
      List(lhsMov, rhsMov, bn, mv)
    }

    op match
      case "Add" => toStr("add")
      case "Sub" => toStr("sub")
      case "LT" => toStr("cmp")
      case "GT" => toStr("cmp")
  }

  def instStr(inst: String, operand: MachineOperand): String = {
    inst + instTy(4)
  }

  def instStr(inst: String, len: Int): String = {
    inst + instTy(len)
  }


  def instTy(size: Int): String = {
    if size == 4 then "l" else "q"
  }

  def getRegASM(len: Int, num: Int) = {
    if (len == 4) {
      numToReg4(num)
    } else if (len == 8) {
      numToReg8(num)
    } else {
      ???
    }
  }

  def regToASM(reg: VReg): String = {
    numToReg4(reg.num)
  }

  def numToReg4(num: Int): String = {
    val name = num match
      case 0 => "eax"
      case 1 => "ebx"
      case 2 => "ecx"
      case 3 => "edx"
      case 4 => "esi"
      case 5 => "edi"
      //      case 6 => "ebp" // ebp
      //      case 7 => "esp" // esp
      case 6 => "r8d"
      case 7 => "r9d"
      case 8 => "r10d"
      case 9 => "r11d"
      case 10 => "r12d"
      case 11 => "r13d"
      case 12 => "r14d"
      case 13 => "r15d"
      case _ => "out"
    "%" + name
  }

  def numToReg8(num: Int): String = {
    val name = num match
      case 0 => "rax"
      case 1 => "rbx"
      case 2 => "rcx"
      case 3 => "rdx"
      case 4 => "rsi"
      case 5 => "rdi"
      case 6 => "rbp"
      case 7 => "rsp"
      case 8 => "r8"
      case 9 => "r9"
      case 10 => "r10"
      case 11 => "r11"
      case 12 => "r12"
      case 12 => "r12"
      case 13 => "r13"
      case 14 => "r14"
      case 15 => "r15"
      case 99 => "rip"
      case _ => "out"
    "%" + name
  }

  // rdi, rsi, rdx, rcx, r8/r8d, r9/r9d
  def paramReg(num: Int, len: Int): String = {
    var name = ""
    if (len == 4) {
      name = num match
        case 0 => "edi"
        case 1 => "esi"
        case 2 => "edx"
        case 3 => "ecx"
        case _ => ???
    } else {
      name = num match
        case 0 => "rdi"
        case 1 => "rsi"
        case 2 => "rdx"
        case 3 => "rcx"
        case _ => ???
    }
    "%" + name
  }
}

