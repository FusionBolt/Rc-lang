package rclang
package codegen

trait Section

val indent = "  "
case class TextSection(var fns: Map[String, List[String]] = Map()) extends Section {
  def asm: String = {
    val decls = ".section .text\n" + fns.keys.map(n => s"${indent}.globl $n\n${indent}.type  $n, @function").mkString("\n") + "\n"
    val fnInst = fns.map((name, inst) => s"$name:\n${inst.map(indent + _).mkString("\n")}").mkString("\n") + "\n"
    decls + fnInst + ident
  }

  def ident = s".LFE0:\n$indent.ident \"RCC: 0.0.1\"\n"
  def addFn(fn: (String, List[String])) = {
    fns = fns.updated(fn._1, fn._2)
  }
}

case class RDataSection(data: List[StrSection] = List()) extends Section {
  def asm = data.map(_.asm + "\n").mkString
}

case class StrSection(count: Int, var strs: List[String] = List()) {
  val decls = ".section .rodata"
  val label = s".LC$count:"
  def strsASM = {
    strs.map(s => s"${indent}.string \"$s\"\n").mkString
  }
  def asm = s"$decls\n$label\n$strsASM\n"
}

case class GNUAssembler() {
  def file: String = ""
  var dataSection: List[String] = List()
  var textSection: List[String] = List()
  var bssSection: List[String] = List()
  //  def global: Unit
  //  def label: Unit
  //  def comment: Unit

  def serialize: String = {
    s".file \"${file}\"\n" + textSection + dataSection
  }

  def dataSec: String = {
    sectionSerialize("data", dataSection)
  }

  def textSec: String = {
    sectionSerialize("section", textSection)
  }

  def sectionSerialize(name: String, section: List[String]): String = {
    s"section .${name}\n" + section.map(indent + _).mkString("\n") + "\n"
  }
}

object GNUASM {
  def toASM(fn: MachineFunction): List[String] = {
    val start = List("pushq %rbp", "movq %rsp, %rbp")
    start:::toASM(fn.instructions)
  }

  private def toASM(insts: List[MachineInstruction]): List[String] = {
    insts.map(toASM)
  }

  def instStr(inst: String, operand: MachineOperand): String = {
    inst + instTy(4)
  }

  def instTy(size: Int): String = {
    if size == 4 then "l" else "q"
  }

  // mov src dst
  private def toASM(inst: MachineInstruction): String = {
    inst match
      case BinaryInst(op, dst, lhs, rhs) => binaryInstToASM(op.toString, dst, lhs, rhs)
      case LoadInst(target, value) => value match
//        case AddrOfValue(relReg) => s"${instStr("lea", relReg)} ${operandToASM(relReg)}, ${operandToASM(target)}"
// todo: when target is label, call param is also need load
        case Label(label) => s"leaq ${label}(%rip), ${operandToASM(target)}"
        case _ => s"${instStr("mov", value)} ${operandToASM(value)}, ${operandToASM(target)}"
      case StoreInst(value, target) => s"${instStr("mov", target)} ${operandToASM(value)}, ${operandToASM(target)}"
//      case DynamicAllocInst(target) => "mov"
      case ReturnInst(value) => s"${instStr("mov", value)} ${operandToASM(value)}, %eax\npopq %rbp\nret"
//      case PushInst(value) => s"${instStr("push", value)} ${operandToASM(value)}"
//      case PopInst(target) => s"${instStr("pop", target)} ${regToASM(target)}"

      case CallInst(target, _, args) => {
        // todo: fix this, reg error
        val argList = args.zipWithIndex.map((value, i) => value match
          case Label(label) => s"leaq ${label}(%rip), ${paramReg(i, 8)}"
          case _ => s"${instStr("mov", value)} ${operandToASM(value)}, ${paramReg(i, 4)}").reverse.mkString("\n")
        val call = s"\ncall $target"
        argList + call
      }
      case InlineASM(content) => content
//      case BranchInst(label) => s"jmp $label"
//      case CondBrInst(cond, trueBranch, falseBranch) => ("cmp")
      case FrameIndexInst(dst, index) => s"movl ${-((index.value + 1)*4)}(%rbp), ${operandToASM(dst)}"
      case x => println(x.getClass.toString); ???
  }

  def binaryInstToASM(op: String, dst: MachineOperand, lhs: MachineOperand, rhs: MachineOperand): String = {
    var infactLhs = lhs
    var infactRhs = rhs
    if(!rhs.isInstanceOf[VReg]) {
      infactLhs = rhs
      infactRhs = lhs
    }
    def toStr(op: String) = {
      val bn = s"${instStr(op, infactLhs)} ${operandToASM(infactLhs)}, ${operandToASM(infactRhs)}\n"
      val mv = s"${instStr("mov", dst)} ${operandToASM(infactRhs)}, ${operandToASM(dst)}"
      bn + mv
    }
    op match
      case "Add" => toStr("add")
      case "Sub" => toStr("sub")
      case "LT" => toStr("cmp")
      case "GT" => toStr("cmp")
  }

  def operandToASM(operand: MachineOperand): String = {
    operand match
      case Imm(value) => "$"+{value.toString}
      case r: VReg => regToASM(r)
//      case RelativeReg(reg, offset) => s"${offsetToASM(offset)}(${regToASM(reg)})"
//      case AddrOfValue(v) => v.toString
      case Label(name) => name
      case _ => ???
  }

//  def offsetToASM(offset: Offset): String = {
//    offset match
//      case Offset.NumOffset(int) => int.toString
//      case Offset.LabelOffset(str) => str
//  }

  def regToASM(reg: VReg): String = {
    reg4ToASM(reg)
//    reg match
//      case ParamReg(num, len) => paramReg(num, len)
//      case _ => if reg.length == 4 then reg4ToASM(reg) else reg8ToASM(reg)
  }

  // rdi, rsi, rdx, rcx, r8/r8d, r9/r9d
  def paramReg(num: Int, len: Int): String = {
    var name = ""
    if(len == 4) {
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

  def reg4ToASM(reg: VReg): String = {
    val name = reg.num match
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

  def reg8ToASM(reg: VReg): String = {
    val name = reg.num match
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
      case 13 => "r13"
      case 14 => "r14"
      case 15 => "r15"
      case 99 => "rip"
      case _ => "out"
    "%" + name
  }
}