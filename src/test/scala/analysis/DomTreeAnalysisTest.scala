package rclang
package analysis

import mir.{DomTreeBuilder, *}
import ty.NilType
import tools.DumpManager

import scala.collection.mutable.LinkedHashSet
class DomTreeAnalysisTest extends RcTestBase {
  var bbs: BBsType = _
  before {
    bbs = mkBBs(
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
  }

  describe("DomTreeBuild") {
    it("success") {
      println(bbs)
      val fn = Function("fn", NilType, List(), bbs.values.head, bbs.values.toList)
      CFGRender.rendFn(fn, "DomTree.dot")
      val predMap = predecessorsMap(fn.bbs)
//      val nodes = dfsBasicBlocks("entry")
      val nodes = bbs("entry")::bbs.values.toList:::List(bbs("exit"))
      val builder = DomTreeBuilder()
      val domain = builder.computeImpl(LinkedHashSet.from(nodes), predMap, "entry")
      val tree = builder.makeTree(domain, fn)
      given DomTree = tree

      "1".noOtherDom
      "2" isDom ("1")
      "3" isDom ("1")
      "4" isDom ("1", "3")
      "5" isDom ("1", "3", "4")
      "6" isDom ("1", "3", "4")
      "exit" isDom ("1")

      val idoms = iDomCompute(LinkedHashSet.from(nodes), domain, "entry")
      idoms("1").name shouldBe "entry"
      idoms("2").name shouldBe "1"
      idoms("3").name shouldBe "1"
      idoms("4").name shouldBe "3"
      idoms("5").name shouldBe "4"
      idoms("6").name shouldBe "4"
      idoms("exit").name shouldBe "1"
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
