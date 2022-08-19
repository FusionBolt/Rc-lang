package rclang
package analysis

import mir.*

import org.scalatest.BeforeAndAfter
import pass.{Analysis, AnalysisManager, getAnalysisResult}
import compiler.Driver.{getSrc, parse, simplify, typeProc}
import analysis.Analysis.given_LoopAnalysis
import tools./

import java.io.File

def getFirstFn(srcPath: String) = {
  val src = getSrc(srcPath)
  val ast = parse(src)
  val (typedModule, table) = typeProc(ast)
  val mirMod = ToMIR(table).proc(typedModule)
  mirMod.fnTable.values.head
}

def getDemoFirstFn(name: String) = {
  getFirstFn("demo" / name)
}

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
        val fn = getDemoFirstFn("while.rc")
        val loopInfo = getLoopInfo(fn)
        loopInfo.loops should be (Map(fn.getBB("1") -> Loop(List("1, 2").map(fn.getBB))))
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
