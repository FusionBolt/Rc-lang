package rclang
package transform

import mir.*
import mir.MIRTestUtil.*

import rclang.pass.AnalysisManager

class LoopMergeTest extends RcTestBase {
  describe("simple loop merge") {
    it("stand loop") {
      val bbs = mkBBs(
        "entry1" -> "header1",
        "header1" -> "body1",
        "body1" -> "latch1",
        "latch1" -> "header1",
        "latch1" -> "entry2",

        "entry2" -> "header2",
        "header2" -> "body2",
        "body2" -> "latch2",
        "latch2" -> "header2",
        "latch2" -> "exit")
      val fn = MakeBBsFunction(bbs)
      //      LoopMerge().run(fn, AnalysisManager())
      //      val loopInfo = getLoopInfo(fn)
      //      loopInfo.loops should be(Map(
      //        fn.getBB("header1") ->
      //          Loop(List("header1", "body1", "body2", "latch1", "exit").map(fn.getBB))))
    }
  }
}
