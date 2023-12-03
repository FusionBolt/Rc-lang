package rclang
package analysis

import mir.{DomTreeNode, Function}
import pass.{Analysis, AnalysisManager}
import rclang.analysis.Analysis.given_DomTreeAnalysis

case class DomFrontier() extends Analysis[Function] {
  override type ResultT = Map[DomTreeNode, Set[DomTreeNode]]
  override def run(irUnit: Function, AM: AnalysisManager[Function]): ResultT = {
    val domTree = AM.getResult[DomTreeAnalysis](irUnit)
    var df = Map[DomTreeNode, Set[DomTreeNode]]()
    for (n <- irUnit.bbs) {
      df = df.updated(domTree(n), Set[DomTreeNode]())
    }

    for (n <- irUnit.bbs.map(domTree(_))) {
      if(n.preds.length > 1) {
        for (p <- n.preds.map(domTree(_))) {
          var runner = p
          while(runner != n.iDom) {
            df = df.updated(runner, df(runner) + n)
            runner = runner.iDom
          }
        }
      }
    }
    
    df
  }
}