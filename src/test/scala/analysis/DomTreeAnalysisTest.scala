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
      "2" -> "exit",
      "3" -> "4",
      "4" -> "5",
      "4" -> "6",
      "5" -> "exit",
      "6" -> "4"
    )
  }

  describe("DomTreeBuild") {
    it("success") {
      // todo:fn entry should not null
      val fn = Function("fn", NilType, List(), bbs.values.toList.sortBy(_.name))
      CFGRender.rendFn(fn, "DomTree.dot")
      val predMap = predecessorsMap(fn.bbs)
      val nodes = dfsBasicBlocks("entry")
      val tree = DomTreeBuilder().compute(nodes, predMap, "entry")
      given DomTree = tree

      "1".noOtherDom
      "2" isDom ("1")
      "3" isDom ("1")
      "4" isDom ("1", "3")
      "5" isDom ("1", "3", "4")
      "6" isDom ("1", "3", "4")
    }
  }

  extension (n: String) {
    def isDom(children: String*)(using tree: DomTree): Unit = {
      val expect = (children.toList:::List(n, "entry")).toSet
      tree(n).children.map(_.name).toSet should be(expect)
    }

    def noOtherDom(using tree: DomTree): Unit = {
      isDom()
    }
  }

  implicit def strToBB(str: String): BasicBlock = bbs(str)
}
