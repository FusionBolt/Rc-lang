package rclang
package analysis

import mir.*

import org.scalatest.BeforeAndAfter
import pass.{Analysis, AnalysisManager, getAnalysisResult}
import compiler.Driver.{getSrc, parse, simplify, typeProc}
import analysis.Analysis.given_LoopAnalysis


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
      val fn = getDemoFirstFn("control/while.rc")
      val loopInfo = getLoopInfo(fn)
      loopInfo.loops should be (Map(fn.getBB("1") -> Loop(List("1", "2").map(fn.getBB))))
    }

    it("break") {

    }

    it("continue") {

    }
  }

  describe("nested loop") {
    it("ok") {

    }
  }

}
