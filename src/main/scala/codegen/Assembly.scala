package rclang
package codegen
import scala.sys.process._

trait Section

val indent = "  "
case class TextSection(var fns: Map[String, List[String]] = Map()) extends Section {
  override def toString: String = {
    val decls = ".section .text\n" + fns.keys.map(n => s"${indent}.globl $n\n${indent}.type  $n, @function").mkString("\n") + "\n"
    val fnInst = fns.map((name, inst) => s"$name:\n${inst.map(indent + _).mkString("\n")}").mkString("\n") + "\n"
    decls + fnInst + ident
  }

  def ident = s".LFE0:\n$indent.ident \"RCC: 0.0.1\"\n"
  def addFn(fn: (String, List[String])) = {
    // todo:refactor
    fns = fns.updated(fn._1, fn._2)
  }
}

case class Assembly() {
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

  def toASM(inst: MachineInst): String = {
    inst match
      case ArithInst(op, lhs, rhs) => arithInstToASM(op, lhs, rhs)
      case LoadInst(target, value) => s"movl ${operandToASM(target)}, ${operandToASM(value)}"
      case StoreInst(value, target) => s"movl ${operandToASM(target)}, ${operandToASM(value)}"
      case DynamicAllocInst(target) => "" // todo:error
      case ReturnInst(value) => "ret"
      case PushInst(value) => s"pushl ${operandToASM(value)}"
      case PopInst(target) => ???
      case CallInst(target) => s"call $target"
      case _ => ???
  }

  def arithInstToASM(op: String, lhs: MachineOperand, rhs: MachineOperand): String = {
    op match
      // todo:result in where??
      case "Add" => s"addl ${operandToASM(lhs)}, ${operandToASM(rhs)}"
      case _ => ???
  }

  def operandToASM(operand: MachineOperand): String = {
    operand match
      case Imm(value) => "$"+{value.toString}
      case r: Reg => regToASM(r)
      case RelativeReg(reg, offset) => s"$offset(${regToASM(reg)})"
      case _ => ???
  }

  def regToASM(reg: Reg): String = {
    val name = reg.number match
      case 0 => "eax"
      case 1 => "ebx"
      case 2 => "ecx"
      case 3 => "edx"
      case 4 => "esi"
      case 5 => "edi"
      case 6 => "ebp"
      case 7 => "esp"
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
}

def as(srcPath: String, destPath: String): Unit = {
  val args = List(srcPath, "-o", destPath)
  val out = s"as ${args.mkString(" ")}".!!
}