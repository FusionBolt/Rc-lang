package rclang
package mir

import ty.NilType
import scala.collection.mutable.LinkedHashSet

trait MIRTestUtil {
  var bbs: BBsType = mkBBs(
    "entry" -> "1",
    "1" -> "2",
    "2" -> "exit",
    "1" -> "3",
    "3" -> "4",
    "4" -> "5",
    "5" -> "exit",
    "4" -> "6",
    "6" -> "4"
  )

  def mkTree = {
    val fn = Function("fn", NilType, List(), bbs.values.head, bbs.values.toList)
    val predMap = predecessorsMap(fn.bbs)
    val nodes = bbs("entry") :: bbs.values.toList ::: List(bbs("exit"))
    val builder = DomTreeBuilder()
    builder.compute(LinkedHashSet.from(nodes), predMap, bbs("entry"))
  }

  implicit def strToBB(str: String): BasicBlock = bbs(str)
}
