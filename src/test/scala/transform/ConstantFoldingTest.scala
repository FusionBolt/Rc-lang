package rclang
package transform

import pass.{AnalysisManager, PassManager}
import mir.*

class ConstantFoldingTest extends RcTestBase {

  describe("ConstantFoldingTest") {
    it("should run") {
      val fn = getDemoFirstFn("constant_folding.rc")
      ConstantFolding().run(fn, AnalysisManager())
      println(fn)
    }
  }
}
