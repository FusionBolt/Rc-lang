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
    CFGRender().rend("CFGTest", DumpManager.getDumpRoot, bbs.values.toList)
  }

  describe("canReach") {
    it("success") {
      canReach(bbs("1"), bbs("2")) should be(true)
      canReach(bbs("1"), bbs("3")) should be(true)
      canReach(bbs("1"), bbs("4")) should be(true)
      canReach(bbs("4"), bbs("1")) should be(false)
    }
  }

  describe("dfsBasicBlocks") {

  }
}
