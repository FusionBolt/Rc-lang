package rclang
package codegen

import java.io.PrintWriter

class MachineIRPrinter {
  def print(mfs: List[MachineFunction]): Unit = mfs.foreach(print)

  def printToWriter(mf: MachineFunction, writer: PrintWriter): Unit = {
    writer.write(toStr(mf))
  }

  def print(mf: MachineFunction): Unit = {
    val content = toStr(mf)
    println(content)
  }

  private def toStr(mf: MachineFunction): String = {
    (List(mf.name):::mf.bbs.flatMap(toStr)).map(_ + "\n").mkString
  }

  private def toStr(mbb: MachineBasicBlock): List[String] = {
    List(mbb.name):::mbb.instList.map(toStr)
  }

  private def toStr(inst: MachineInstruction): String = {
    s"${inst.getClass.getName.split('.').last} ${inst.operands} = "
  }
}
