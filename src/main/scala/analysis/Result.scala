package rclang
package analysis

import mir.*

object Analysis:
  given BasicAA with {
    type ResultT = AAResult
  }

  given DomTreeAnalysis with {
    type ResultT = DomTree
  }

  given LoopAnalysis with {
    type ResultT = LoopInfo
  }

