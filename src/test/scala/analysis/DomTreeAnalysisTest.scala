package rclang
package analysis

import mir.{DomTreeBuilder, *}
import ty.NilType
import tools.DumpManager

class DomTreeAnalysisTest extends RcTestBase with MIRTestUtil {
  describe("DomTreeBuild") {
    it("success") {
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

  extension (n: String) {
    def isDom(children: String*)(using tree: DomTree): Unit = {
      val expect = (children.toList ::: List(n, "entry")).toSet
      tree(n).children.map(_.name).toSet should be(expect)
    }

    def isIDom(child: String)(using tree: DomTree): Unit = {
      tree(child) idom tree(n)
    }

    def noOtherDom(using tree: DomTree): Unit = {
      isDom()
    }
  }
}
