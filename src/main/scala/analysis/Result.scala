package rclang
package analysis

import mir.*

object Analysis:
  given DomTreeAnalysis with {
    type ResultT = DomTree
  }

  given LoopAnalysis with {
    type ResultT = LoopInfo
  }
  
  given DomFrontierAnalysis with {
    type ResultT = Map[DomTreeNode, Set[DomTreeNode]]
  }

