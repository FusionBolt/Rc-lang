package rclang
package codegen

import pass.*

class PhiEliminate extends Transform[MachineFunction] {
  def run(fn: MachineFunction, am: AnalysisManager[MachineFunction]) = {
    fn.bbs.foreach(bb => {
      // 1. find all phi
      val phis = bb.instList
        .filter(inst => inst.isInstanceOf[PhiInst])
        .map(inst => inst.asInstanceOf[PhiInst])
      // 2. replace with x0 = value
      phis.foreach(eliminate(_, bb))
    })
  }

  // make copy for every income
  def eliminate(phiInst: PhiInst, basicBlock: MachineBasicBlock) = {
    val target = VReg(basicBlock.parent.instructions.length)
    phiInst.incomings.foreach((v, mbb) => {
      val parent = v.instParent
      val store = StoreInst(target.dup, v).setOrigin(parent.origin)
      mbb.insertAt(store, mbb.instList.length - 1)
      println(mbb.instList)
    })
    val st = StoreInst(phiInst.dst, target.dup).setOrigin(phiInst.origin)
    basicBlock.insertAtFirst(st)
    // remove phi
    phiInst.removeFromParent()
  }
}
