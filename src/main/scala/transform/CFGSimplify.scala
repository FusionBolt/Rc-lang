package rclang
package transform

import mir.*
import pass.{AnalysisManager, Transform}

def removeUnreachedBasicBlock(IRUnit: Function): Unit = {
  val bbs = dfsBasicBlocks(IRUnit.entry).toSet
  val newBBs = IRUnit.bbs.filter(bbs.contains)
  IRUnit.bbs = newBBs
}

class CFGSimplify() extends Transform[Function] {
  override def run(IRUnit: Function, AM: AnalysisManager[Function]): Unit = {
    removeUnreachedBasicBlock(IRUnit)
  }
}
