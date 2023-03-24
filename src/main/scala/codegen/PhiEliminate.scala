package rclang
package codegen

class PhiEliminate {
  def run(fn: MachineFunction) = {
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
    println(phiInst)
    val regs = phiInst.incomings.map((v, mbb) => {
      val target = VReg(-1)
      // insert copy
      val store = StoreInst(v, target)
      store.origin = v.instParent.origin
      mbb.insert(store)
      target
    })
    regs.foreach(reg => {
      val load = LoadInst(VReg(-1), reg)
      load.origin = phiInst.origin
      basicBlock.insertAtFirst(load)
    })
    // remove phi
    phiInst.removeFromParent()
  }
}
