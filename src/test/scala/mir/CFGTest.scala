package rclang
package mir

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.BeforeAndAfter
import rclang.tools.DumpManager

class CFGTest extends RcTestBase {
  def mkBB(name: String): BasicBlock = {
    val bb = BasicBlock(name, List(MultiSuccessorsInst()))
    bb
  }

  type BBsType = Map[String, BasicBlock]
  def mkBBs(connections: (String, String)*): BBsType = {
    val set = connections.foldLeft(Set[BasicBlock]())((s, e) => s + mkBB(e._1) + mkBB(e._2))
    val map = set.map(s => s.name -> s).toMap
    connections.foreach((begin, end) => {
      map(begin).terminator.asInstanceOf[MultiSuccessorsInst].add(map(end))
    })
    map
  }

  var bbs: BBsType = null
  before {
    bbs = mkBBs(
    "1" -> "2",
    "1" -> "3",
    "2" -> "4",
    "3" -> "4",
    )
    println(bbs.values.map(_.name))
    rendDot(bbs.values.toList, "CFGTest", DumpManager.getDumpRoot)
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
