package rclang
package analysis

import mir.*

import org.scalatest.BeforeAndAfter
import pass.{Analysis, AnalysisManager, getAnalysisResult}
import compiler.Driver.{getSrc, parse, simplify, typeProc}
import analysis.Analysis.given_LoopAnalysis

import rclang.ty.NilType

import java.io.File

def getLoopInfo(fn: Function) = {
  simplify(fn)
  getAnalysisResult[Function, LoopAnalysis](fn)
}

def mkLoop(header: String)(bbs: String*) = {
  Loop((header::bbs.toList).map(BasicBlock(_)))
}

class LoopAnalysisTest extends RcTestBase {
  describe("normal loop") {
    it("simple") {
      val bbs = mkBBs(
        "entry" -> "header",
        "header" -> "body",
        "body" -> "exit",
        "body" -> "header")

      val bbList = bbs.values.toList
      val fn = new Function("name", NilType, List(), bbs("entry"), bbList)
      val loopInfo = getLoopInfo(fn)
      loopInfo.loops should be (Map(fn.getBB("header") -> Loop(List("header", "body").map(fn.getBB))))
    }

    it("withLatch") {
      val bbs = mkBBs(
        "entry" -> "header",
        "header" -> "body",
        "body" -> "latch",
        "latch" -> "header",
        "latch" -> "exit")
      val bbList = bbs.values.toList
      val fn = new Function("name", NilType, List(), bbs("entry"), bbList)
      val loopInfo = getLoopInfo(fn)
      loopInfo.loops should be(Map(fn.getBB("header") -> Loop(List("header", "body", "latch").map(fn.getBB))))
    }

    it("continue") {

    }
  }

  describe("nested loop") {
    it("ok") {

    }
  }

}
