package rclang
package analysis

import pass.{Analysis, AnalysisManager}
import mir.*
import analysis.Analysis.given_DomTreeAnalysis

class LoopAnalysis extends Analysis[Function] {
  override type ResultT = LoopInfo

  override def run(irUnit: Function, AM: AnalysisManager[Function]) = {
    val domTree = AM.getResult[DomTreeAnalysis](irUnit)
    println("-------begin------")
    var loopRanges = Map[BasicBlock, List[BasicBlock]]()
    domTree.visit(node => {
      println(s"node:${node.name} start")
      node.basicBlock.successors.find(succ => {
        // back edge: successor dom current node
        domTree(succ) dom node
      }) match
        case Some(succ) => {
          // todo: 1. nest loop
          // todo: 2. scc
          // todo: 3. multi back edge
          println(s"succ:${succ.name} node:${node.name}")
          // 从succ出发到node到所有bb都是这个循环体的部分
          // 因为存在支配关系，因此从succ，也就是header开始的所有通路都通向node
          val bbsInLoop = loopBasicBlocks(succ, node.basicBlock)
          loopRanges = loopRanges + (succ -> bbsInLoop)
        }
        case None =>
      println("end")
    })
    println("-------end------")
    // from key to value

    println(loopRanges)
    val result = loopRanges.map((header, list) => {
      println(s"header:${header.name}")
      (header -> Loop(list))
    }).toMap
    println(result)
    LoopInfo(result)
  }
}
