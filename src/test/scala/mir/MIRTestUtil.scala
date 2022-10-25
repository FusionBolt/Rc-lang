package rclang
package mir

import ty.NilType

import scala.collection.mutable
import scala.collection.mutable.LinkedHashSet

trait MIRTestUtil {
  def mkTree(using bbs: BBsType) = {
    val fn = Function("fn", NilType, List(), bbs.values.head, bbs.values.toList)
    CFGRender.rendFn(fn, "bbs.dot")
    val predMap = predecessorsMap(fn.bbs)
    val nodes = bbs("entry") :: bbs.values.toList ::: List(bbs("exit"))
    val builder = DomTreeBuilder()
    builder.compute(LinkedHashSet.from(nodes), predMap, bbs("entry"))
  }

  implicit def strToBB(str: String)(using bbs: BBsType): BasicBlock = bbs(str)
}
