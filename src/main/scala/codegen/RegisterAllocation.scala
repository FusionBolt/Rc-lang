package rclang
package codegen

import pass.{AnalysisManager, Transform}

class StackRegisterAllocation extends Transform[MachineFunction] {
  def debug(str: String): Unit = {
    if(false) {
      println(str)
    }
  }

  def run(mf: MachineFunction, am: AnalysisManager[MachineFunction]) = {
    debug(s"generate for ${mf.name}")
    val frameInfo = mf.frameInfo
    var regMap = Map[VReg, StackItem]()
    val allVReg = mf.instructions.flatMap(m => m.operands).map(_ match
      case v: VReg => Some(v)
      case _ => None).filter(v => v.isDefined && !v.get.force).map(_.get)
    allVReg.foreach(reg => {
      debug(reg.toString)
      debug(reg.instParent.operands.toString)
      val item = regMap.get(reg) match {
        case Some(value) => debug("yes"); value
        case None => {
          // update FrameInfo
          debug("no")
          val tmpItem = frameInfo.addItem(TmpItem(reg.size))
          regMap = regMap.updated(reg, tmpItem)
          tmpItem
        }
      }
      debug(s"$reg -> $item")
      // replace operand
      val frameIndex = FrameIndex(item.offset, reg.size)
      reg.replaceFromParent(frameIndex)
      debug(frameIndex.instParent.operands.toString)
    })
  }
}