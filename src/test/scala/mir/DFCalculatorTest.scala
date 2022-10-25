package rclang
package mir

import ty.NilType
import scala.collection.mutable.LinkedHashSet

class DFCalculatorTest extends RcTestBase with MIRTestUtil {
//  override val bbs = mkBBs(
//    "entry" -> "0",
//    "0" -> "1",
//    "1" -> "2",
//    "2" -> "3",
//    "3" -> "4",
//    "3" -> "12",
//    "4" -> "5",
//    "4" -> "1",
//    "5" -> "8",
//    "5" -> "6",
//    "6" -> "7",
//    "6" -> "4",
//    "7" -> "exit",
//    "8" -> "9",
//    "8" -> "1",
//    "9" -> "10",
//    "10" -> "13",
//    "10" -> "11",
//    "11" -> "9",
//    "11" -> "8",
//    "12" -> "2",
//    "12" -> "1",
//    "13" -> "9",
//    "13" -> "8",
//  )

  describe("build") {
    val order = List(0, 1, 2, 5, 6, 8, 7, 3, 4)
    val bbs = mkBBsByOrder(
      order,
      "entry" -> "0",
      "0" -> "1",
      "1" -> "2",
      "2" -> "3",
      "1" -> "5",
      "5" -> "6",
      "6" -> "7",
      "5" -> "8",
      "8" -> "7",
      "7" -> "3",
      "3" -> "1",
      "3" -> "4",
      "4" -> "exit",
    )
    it("succeed") {
      given BBsType = bbs
      val tree = mkTree
      given DomTree = tree
      iDomCompute(tree, "entry")
      val result = DFCalculator(tree).run("1")
      assert(result.toSet == List("1", "3", "7").map(tree(_).basicBlock).toSet)
    }
  }
}
