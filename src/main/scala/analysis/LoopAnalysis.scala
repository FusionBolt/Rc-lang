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
//      val result = node.children.filter(child => child.children.map(_.basicBlock).contains(node))
//      // 多个回边则是最大回边？？多重循环怎么办
//      loopRanges = loopRanges +

    // 多重循环其实也无所谓


      node.basicBlock.successors.find(succ => {
        // is back edge
        domTree(succ) dom node
      }) match
        case Some(value) => {
          println(s"succ:${value.name} node:${node.name}")
          loopRanges = loopRanges.get(value) match
            case Some(nodes) => loopRanges.updated(value, nodes :+ node.basicBlock)
            case None => loopRanges + (value -> List(node.basicBlock))
//              loopRanges = loopRanges + (value -> node.basicBlock)
        }
        case None =>
    })
    println("-------end------")
    // from key to value

    val result = loopRanges.map((header, list) => {
      println(s"header:${header.name}")
      (header -> Loop(header::list.filter(canReach(_, header))))
    }).toMap
    println(result)
    // 被node支配的所有结点中的所有回边
    // 可能存在多个回边，因此需要MapBBTo[BB]

    // 从header开始dfs,每次到回边就停止，否则就加到里面。但是这个样子多个回边就有问题，所以要一直到最后一个回边？？
    // header所支配的结点里面能够到达header的就没问题？？

    // 如果嵌套的循环，那么这个loop该怎么表示呢
    LoopInfo(result)
    // 如果多个回边呢
    // 存在回边，如何判断回边？回边的支配性如何
    // 判断SCC的算法
  }
}
