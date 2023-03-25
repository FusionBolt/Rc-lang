package rclang
package codegen

class MachineIRPrinter {
  def print(mfs: List[MachineFunction]): Unit = mfs.foreach(print)
  
  def print(mf: MachineFunction): Unit = {
    println(mf.name)
    mf.bbs.foreach(print)
  }

  def print(mbb: MachineBasicBlock): Unit = {
    println(mbb.name)
    mbb.instList.foreach(print)
  }

  def print(inst: MachineInstruction): Unit = {
    println(s"${inst.getClass.getName.split('.').last} ${inst.dstList} = ${inst.ops}")
  }
}
