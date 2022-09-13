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

case class GNUAssembler() extends Assembler {
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
  def toASM(fn: MachineFunction): String = {
    toASM(fn.instructions)
  }

  def toASM(insts: List[MachineInst]): String = {
    insts.map(toASM).mkString("\n")
  }

  def instStr(inst: String, operand: MachineOperand): String = {
    inst + instTy(sizeof(operand))
  }

  def instTy(size: Int): String = {
    if size == 4 then "l" else "q"
  }

  def toASM(inst: MachineInst): String = {
    inst match
      case ArithInst(op, lhs, rhs) => arithInstToASM(op, lhs, rhs)
      case LoadInst(target, value) => value match
        case AddrOfValue(relReg) => s"${instStr("lea", relReg)} ${operandToASM(relReg)}, ${operandToASM(target)}"
        case _ => s"${instStr("mov", value)} ${operandToASM(value)}, ${operandToASM(target)}"
      case StoreInst(value, target) => s"${instStr("mov", target)} ${operandToASM(value)}, ${regToASM(target)}"
      case DynamicAllocInst(target) => ???
      case ReturnInst(value) => "ret"
      case PushInst(value) => s"${instStr("push", value)} ${operandToASM(value)}"
      case PopInst(target) => s"${instStr("pop", target)} ${regToASM(target)}"
      case CallInst(target) => s"call $target"
      case InlineASM(content) => content
      case _ => ???
  }

  def arithInstToASM(op: String, lhs: MachineOperand, rhs: MachineOperand): String = {
    op match
      case "Add" => s"${instStr("add", lhs)} ${operandToASM(lhs)}, ${operandToASM(rhs)}"
      case _ => ???
  }

  def operandToASM(operand: MachineOperand): String = {
    operand match
      case Imm(value) => "$"+{value.toString}
      case r: Reg => regToASM(r)
      case RelativeReg(reg, offset) => s"${offsetToASM(offset)}(${regToASM(reg)})"
      case AddrOfValue(v) => v.toString
      case Label(name) => name
      case _ => ???
  }

  def offsetToASM(offset: Offset): String = {
    offset match
      case Offset.NumOffset(int) => int.toString
      case Offset.LabelOffset(str) => str
  }

  def regToASM(reg: Reg): String = {
    reg match
      case ParamReg(num, len) => paramReg(num, len)
      case _ => if reg.length == 4 then reg4ToASM(reg) else reg8ToASM(reg)
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

  def reg4ToASM(reg: Reg): String = {
    val name = reg.number match
      case 0 => "eax"
      case 1 => "ebx"
      case 2 => "ecx"
      case 3 => "edx"
      case 4 => "esi"
      case 5 => "edi"
      case 6 => "ebp" // ebp
      case 7 => "esp" // esp
      case 8 => "r8d"
      case 9 => "r9d"
      case 10 => "r10d"
      case 11 => "r11d"
      case 12 => "r12d"
      case 13 => "r13d"
      case 14 => "r14d"
      case 15 => "r15d"
      case _ => "out"
    "%" + name
  }

  def reg8ToASM(reg: Reg): String = {
    val name = reg.number match
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