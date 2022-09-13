package rclang
package mir

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.BeforeAndAfter
import tools.DumpManager
import mir.*

class CFGTest extends RcTestBase {
  var bbs: BBsType = null
  before {
    bbs = mkBBs(
    "1" -> "2",
    "1" -> "3",
    "2" -> "4",
    "3" -> "4",
    )
    println(bbs.values.map(_.name))
    CFGRender.rendBBs(bbs.values.toList, "CFGTest")
  }

  describe("canReach") {
    it("success") {
      canReach(bbs("1"), bbs("2")) should be(true)
      canReach(bbs("1"), bbs("3")) should be(true)
      canReach(bbs("1"), bbs("4")) should be(true)
      canReach(bbs("4"), bbs("1")) should be(false)
    }
  }

  describe("predecessors") {
    it("succ") {
      predecessors(bbs("4"), bbs.values.toList) should be(Set(bbs("2"), bbs("3")))
    }
  }

  describe("dfsBasicBlocks") {
    it("succ") {
      dfsBasicBlocks(bbs("1")) should be(List(bbs("1"), bbs("2"), bbs("4"), bbs("3")))
    }
  }
}
