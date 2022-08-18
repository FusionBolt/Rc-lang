package rclang
package analysis

import pass.{Analysis, AnalysisManager}
import mir.*
import analysis.Analysis.given_DomTreeAnalysis

class LoopAnalysis extends Analysis[Function] {
  override type ResultT = Loop

  override def run(irUnit: Function, AM: AnalysisManager[Function]) = {
    val domTree = AM.getResult[DomTreeAnalysis](irUnit)
    println("-------begin------")
    var loopRanges = Map[BasicBlock, BasicBlock]()
    domTree.visit(node => {
      // todo:succ.size should 1?
      node.basicBlock.successors.find(succ => {
        domTree(succ) dom node
      }) match
        case Some(value) => loopRanges = loopRanges + (value -> node.basicBlock)
        case None =>
    })
    println("-------end------")
    // from key to value

    Loop(List())
    // 如果多个回边呢
    // 存在回边，如何判断回边？回边的支配性如何
    // 判断SCC的算法
  }
}
