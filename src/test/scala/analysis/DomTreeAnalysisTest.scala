package rclang
package analysis

import mir.{DomTreeBuilder, *}
import ty.NilType
import tools.DumpManager

class DomTreeAnalysisTest extends RcTestBase with MIRTestUtil {
  describe("DomTreeBuild") {
    val bbs = mkBBs(
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
    it("success") {
      given BBsType = bbs
      val tree = mkTree
      given DomTree = tree

      "1".noOtherDom
      "2" isDom ("1")
      "3" isDom ("1")
      "4" isDom("1", "3")
      "5" isDom("1", "3", "4")
      "6" isDom("1", "3", "4")
      "exit" isDom ("1")

      // todo: 正反关系
      val idoms = iDomCompute(tree, "entry")
      "1" isIDom "entry"
      "2" isIDom "1"
      "3" isIDom "1"
      "4" isIDom "3"
      "5" isIDom "4"
      "6" isIDom "4"
      "exit" isIDom "1"
    }
  }

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
      "0" isDom ("0")
      "1" isDom ("0", "1")
      "2" isDom ("0", "1", "2")
      "3" isDom ("0", "1", "3")
      "4" isDom ("0", "1", "3", "4")
      "5" isDom ("0", "1", "5")
      "6" isDom ("0", "1", "5", "6")
      "7" isDom ("0", "1", "5", "7")
      "8" isDom ("0", "1", "5", "8")
    }
  }

  extension (n: String) {
    def isDom(children: String*)(using tree: DomTree)(using bbs: BBsType): Unit = {
      val expect = (children.toList ::: List(n, "entry")).toSet
      tree(n).children.map(_.name).toSet should be(expect)
    }

    def isIDom(child: String)(using tree: DomTree)(using bbs: BBsType): Unit = {
      tree(child) idom tree(n)
    }

    def noOtherDom(using tree: DomTree)(using bbs: BBsType): Unit = {
      isDom()
    }
  }
}
