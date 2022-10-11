package rclang
package transform

import pass.{AnalysisManager, PassManager}
import mir.*
import tools.RcLogger.*

class CSETest extends RcTestBase {
  describe("simple cse") {
    it("should run") {
      val fn = getOptDemoFirstFn("cse.rc")
      CSE().run(fn, AnalysisManager())
      logf("after_cse.txt", fn)
      assert(fn.instructions.count(_.isInstanceOf[Binary]) == 2)
      assert(fn.instructions.count(_.isInstanceOf[Load]) == 1)
    }
  }
}
