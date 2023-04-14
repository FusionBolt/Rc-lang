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
//    println(phiInst)
    var index = 0
    val regs = phiInst.incomings.map((v, mbb) => {
      val target = VReg(basicBlock.parent.instructions.length + index)
      index += 1
      // insert copy
      val testValue = v
      val parent = v.instParent
      val store = StoreInst(target, v).setOrigin(parent.origin)
      mbb.insert(store)
      target.dup
    })
    regs.foreach(reg => {
//      val load = LoadInst(VReg(reg.num), reg)
      val phiTarget = VReg(basicBlock.parent.instructions.length + index)
      index += 1
      val st = StoreInst(phiTarget, reg).setOrigin(phiInst.origin)
      basicBlock.insertAtFirst(st)
    })
    // remove phi
    phiInst.removeFromParent()
  }
}
