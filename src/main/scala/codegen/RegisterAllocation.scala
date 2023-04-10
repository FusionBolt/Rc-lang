package rclang
package codegen

class StackRegisterAllocation {
  def run(mf: MachineFunction): Unit = {
    println(s"generate for ${mf.name}")
    val frameInfo = mf.frameInfo
    var regMap = Map[VReg, StackItem]()
    val allVReg = mf.instructions.flatMap(m => m.operands).map(_ match
      case v: VReg => Some(v)
      case _ => None).filter(_.isDefined).map(_.get)
    allVReg.foreach(reg => {
      println(reg.instParent.operands)
      val item = regMap.get(reg) match {
        case Some(value) => println("yes"); value
        case None => {
          // update FrameInfo
          println("no")
          val tmpItem = frameInfo.addItem(TmpItem(4))
          regMap = regMap.updated(reg, tmpItem)
          tmpItem
        }
      }
      println(s"$reg -> $item")
      // replace operand
      val frameIndex = FrameIndex(item.offset)
      reg.replaceFromParent(frameIndex)
      println(frameIndex.instParent.operands)
      println()
    })
  }
}