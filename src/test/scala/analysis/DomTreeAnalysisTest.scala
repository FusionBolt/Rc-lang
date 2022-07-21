package rclang
package analysis

import mir.*
import ty.NilType

import tools.DumpManager
class DomTreeAnalysisTest extends RcTestBase {
  var bbs: BBsType = _
  before {
    bbs = mkBBs(
      "entry" -> "1",
      "1" -> "2",
      "1" -> "3",
      "2" -> "4",
      "3" -> "4",
      "4" -> "5",
      "5" -> "7",
      "7" -> "5",
      "7" -> "exit",
      "4" -> "6",
      "6" -> "1",
      "6" -> "exit",
    )
  }

  def compute(bbs: BBsType) = {
    val f = Function("fn", NilType, List(), bbs.values.toList)
    DomTreeBuilder().compute(f)
  }

  describe("ok") {
    it("tt") {
      val r = CFGRender()
      val root = DumpManager.getDumpRoot
      val bbList = bbs.values.toList.sortBy(_.name)

      r.rendBBs("DomTree.dot", root, bbList)
//      val result = compute(bbs)
//      println("test")
    }
  }
}
