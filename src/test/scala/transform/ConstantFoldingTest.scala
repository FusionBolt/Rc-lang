package rclang
package transform

import pass.{AnalysisManager, PassManager}
import mir.*

class ConstantFoldingTest extends RcTestBase {

  describe("ConstantFoldingTest") {
    it("should run") {
      val fn = getDemoFirstFn("constant_folding.rc")
      ConstantFolding().run(fn, AnalysisManager())
      assert(!fn.instructions.exists(_.isInstanceOf[Binary]))
      assert(fn.instructions.takeRight(2).head.getOperand(0).asInstanceOf[Integer].value == 2)
    }
  }
}
