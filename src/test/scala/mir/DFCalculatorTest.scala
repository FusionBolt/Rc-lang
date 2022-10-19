package rclang
package mir

import ty.NilType
import scala.collection.mutable.LinkedHashSet

class DFCalculatorTest extends RcTestBase with MIRTestUtil {
  describe("simple") {
    it("succeed") {
      val tree = mkTree
      iDomCompute(tree, "entry")
      val result = DFCalculator(tree).run("1")
      assert(result == List(bbs("4")))
    }
  }
}
