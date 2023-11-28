package rclang
package mir

import ty.NilType

import scala.collection.mutable
import scala.collection.mutable.LinkedHashSet
import analysis.Analysis.*
import rclang.compiler.*
import analysis.Analysis.given_LoopAnalysis
import pass.{Analysis, AnalysisManager, getAnalysisResult}

trait MIRTestUtil {
  def mkTree(using bbs: BBsType) = {
    val fn = Function("fn", NilType, List(), bbs.values.head, bbs.values.toList)
    val predMap = predecessorsMap(fn.bbs)
    val nodes = bbs("entry") :: bbs.values.toList ::: List(bbs("exit"))
    val builder = DomTreeBuilder()
    builder.compute(LinkedHashSet.from(nodes), predMap, bbs("entry"))
  }

  implicit def strToBB(str: String)(using bbs: BBsType): BasicBlock = bbs(str)
}

object MIRTestUtil {
  def getLoopInfo(fn: Function): LoopInfo = {
    Driver.simplify(fn)
    getAnalysisResult[Function, analysis.LoopAnalysis](fn)
  }

  def mkLoop(header: String)(bbs: String*) = {
    Loop((header :: bbs.toList).map(BasicBlock(_)))
  }

  def MakeBBsFunction(bbs: BBsType) = {
    val bbList = bbs.values.toList
    val fn = new Function("name", NilType, List(), bbs("entry"), bbList)
    fn
  }
}