package rclang
package transform

import pass.{AnalysisManager, PassManager}
import mir.*
import tools.RcLogger.*

class ConstantFoldingTest extends RcTestBase {

  describe("ConstantFoldingTest") {
    it("should run") {
      val fn = getOptDemoFirstFn("constant_folding.rc")
      ConstantFolding().run(fn, AnalysisManager())
      // alloc + store + return
      assert(!fn.instructions.exists(_.isInstanceOf[Binary]))
      assert(fn.instructions.size == 3)
      assert(fn.instructions.takeRight(2).head.getOperand(0).asInstanceOf[Integer].value == 3)
    }
  }
}
