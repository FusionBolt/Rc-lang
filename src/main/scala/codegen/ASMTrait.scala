package rclang
package codegen

import tools.{Debugger, DumpManager}

import java.io.{File, PrintWriter}

val indent = "  "

case class ASMFile(sections: List[Section] = List()) {
  val ident: String = Def.version
  def write(path: String): Unit = {
    val printer = new PrintWriter(new File(path));
//    println(sectionString)
    printer.write(sectionString)
    printer.write(jmpToMain)
    printer.close()
  }

  private def jmpToMain: String = {
    s".section .text\n  .globl main\n  .type  main, @function\nmain:\n${indent}jmp _ZN6Kernel4mainE1v\n"
  }
  private def sectionString: String = sections.map(_.toASM).mkString("\n") + "\n"
}

case class MFText(asm: List[ASMText], mf: MachineFunction) {
  def toASM: String = {
    asm.map(_ match
      case ASMInstr(instr) => s"$indent$instr"
      case ASMLabel(label) => label
      case _ => Debugger.unImpl).mkString("\n")
  }
}

trait Section {
  def toASM: String = s".section $decl" + "\n" + getASMString

  protected def decl: String

  protected def getASMString: String
}

case class TextSection(mfs: List[MFText]) extends Section {
  private def fnDecls = mfs.map(mfText => s"$indent.globl ${mfText.mf.name}\n$indent.type  ${mfText.mf.name}, @function\n").mkString("\n")
  override def getASMString: String = fnDecls + mfs.map(_.toASM).mkString("\n")

  override def decl: String = ".text"
}

case class StringSection(strTable: Map[String, Label]) extends Section {
  override def getASMString: String = strTable.map((str, label) => s"${label.name}:\n$indent.string \"$str\"").mkString("\n")

  override def decl: String = ".rodata"
}

trait ASMEmiter {
  def emitMF(fm: MachineFunction): MFText

  def emitMBB(mbb: MachineBasicBlock): List[ASMText]

  def emitInstr(instr: MachineInstruction): List[ASMText]
}

def buildASM(mfs: List[MachineFunction], strTable: Map[String, Label]) = {
  val stringSection = StringSection(strTable)
  val mfList = mfs.map(GNUASMEmiter().emitMF)
  val textSection = TextSection(mfList)
  ASMFile(List(stringSection, textSection))
}

def generateASM(mfs: List[MachineFunction], strTable: Map[String, Label], asmPath: String): Unit = {
  val asmFile = buildASM(mfs, strTable)
  asmFile.write(asmPath)
}

trait ASMText {
  def str: String
}

case class ASMLabel(label: String) extends ASMText {
  override def str: String = label
}

case class ASMInstr(instr: String) extends ASMText {
  override def str: String = instr
}

given Conversion[String, ASMInstr] = ASMInstr(_)