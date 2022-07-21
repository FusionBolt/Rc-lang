package rclang
package analysis

import mir.*
import pass.{Analysis, AnalysisManager}


//case class DomTreeAnalysis() extends Analysis[Function] {
//  type ResultT = DomTree
//  def run(irUnit: Function, AM: AnalysisManager[Function]): ResultT = {
//    DomTreeBuilder().compute(irUnit)
//  }
//}