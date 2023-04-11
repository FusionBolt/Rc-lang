package rclang
package codegen

class CodegenIRTest extends RcTestBase {
  describe("TestSetter") {
    it("succ") {
      val src = VReg(1)
      val dst = VReg(2)
      val newSrc = VReg(3)
      val load = LoadInst(dst, src)
      load.addr = newSrc
      assert(load.addr.asInstanceOf[VReg].num == 3)
    }
  }

  describe("TestPatternMatch") {
    it("succ") {
      val src = VReg(1)
      val dst = VReg(2)
      val load = LoadInst(dst, src)
      load match
        case LoadInst(d, s) => d == dst && s == src
        case _ => ???
      val newDst = VReg(3)
      load.dst = newDst
      load match
        case LoadInst(d, s) => d == newDst && s == src
        case _ => ???
    }
  }

  describe("ReplaceFromParent") {
    it("succ") {
      val src = VReg(1)
      val dst = VReg(2)
      val newSrc = VReg(3)
      // todo: change set parent
      val load = LoadInst(dst, src)
      src.replaceFromParent(newSrc)
      assert(newSrc == load.addr)
      assert(newSrc.instParent == load)
    }
  }

  describe("RemoveFromParent") {
    it("succ") {
      val load1 = LoadInst(VReg(0), VReg(1))
      val load2 = LoadInst(VReg(1), VReg(2))
      val store = StoreInst(VReg(1), VReg(3))
      val mbb = MachineBasicBlock(List(load1, load2, store), null, null, "mbb")
      load2.removeFromParent()
      assert(mbb.instList == List(load1, store))
    }
  }

  // todo: do this test
//  describe("getVReg") {
//    it("succ") {
//      val reg = VReg(0)
//      val load = LoadInst(reg, VReg(1))
//      val store = StoreInst(reg, VReg(1))
//      reg.instParent
//    }
//  }
}
