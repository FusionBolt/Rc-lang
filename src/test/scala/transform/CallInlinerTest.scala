package rclang
package transform

import mir.*

import rclang.pass.AnalysisManager

class CallInlinerTest extends RcTestBase {
  describe("simple") {
    it("succ") {
      val args = List(Argument("a", ty.Int32Type), Argument("b", ty.Int32Type))
      val bn = new Binary("Add", args(0), args(1))
      val bb = new BasicBlock("b1", List(bn, new Return(bn)))
      val f = Function("add", ty.Int32Type, args, bb, List(bb))

      val a = Integer(1)
      val b = Integer(2)
      val c = Call(f, List(a, b))
      val d = new Binary("Add", Integer(3), c)
      val mainBB = new BasicBlock("mainBB", List(c, d))
      val main = Function("main", ty.NilType, List(), mainBB, List(mainBB))
      CallInliner().run(main, new AnalysisManager[Function]())
      assert(main.bbs.length == 2)
      assert(!main.instructions.exists(x => x.isInstanceOf[Call]))
    }
  }
}
