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

