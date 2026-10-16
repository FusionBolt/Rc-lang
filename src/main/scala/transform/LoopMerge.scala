package rclang
package transform
import mir.{BasicBlock, *}
import pass.{AnalysisManager, Transform}
import analysis.Analysis.given_LoopAnalysis

// todo: loop rotate
// todo: loop simplify
// cfg simplify
// loop标准化

// 1. collect sequence loop
//    a. simple sequence
//    b. can loop motion??
// 2. can merge??
//    a. check condition
//    b. check Value effect
// 3. do merge
// 4. remove old basicblock
class LoopMerge extends Transform[Function] {
  override def run(fn: Function, am: AnalysisManager[Function]): Unit = {
  }
//    val loopInfo = am.getResult[analysis.LoopAnalysis](fn)
//    loopInfo.loops.foreach((header, loop) => {
//
//    })
//
//    val loop1 = Loop()
//    val loop2 = Loop()
//    if(canMerge(loop1, loop2)) {
//      mergeLoopBody(loop1, loop2)
//      clearOldBB(fn, loop1)
//    }
//  }
//
//  def canMerge(loop1: Loop, loop2: Loop): Boolean = {
//    lazy val subEmpty = loop1.subLoop.empty && loop2.subLoop.empty
//    lazy val sameCond = checkCondition(loop1, loop2)
//    // loop2没有用到loop1中的值
//    lazy val v1 = collectAllValue(loop1)
//    lazy val v2 = collectAllValue(loop2)
//    lazy val notEffect = (v2 - v1) == v2
//    subEmpty && sameCond && notEffect
//  }
//
//  def collectAllValue(loop: Loop): Set[Value] = {
//
//  }
//
//  def mergeLoopBody(loop1: Loop, loop2: Loop): Unit = {
//    // body合并
//    mergeBasicBlock(loop1.body, loop2.body, loop2.latch)
//  }
//
//  def clearOldBB(fn: Function, beforeLoop: Loop) = {
//    val set = Set(beforeLoop.header, beforeLoop.latch, beforeLoop.exit)
//    fn.bbs = fn.bbs.filter(set.contains)
//  }
//
//  def mergeBasicBlock(b1: BasicBlock, b2: BasicBlock, next: BasicBlock) = {
//    val inst = b1.stmts.init :+ b2.stmts.init :+ Branch(next)
//    new BasicBlock(s"${b1.name}_${b2.name}", inst)
//  }
//
//  // i <= 5
//  // todo: check i
//  def checkCondition(loop1: Loop, loop2: Loop): Boolean = {
//    if (checkHeaderCondition(loop1) || checkHeaderCondition(loop2)) {
//      throw new RuntimeException("invalid loop")
//    }
//    val header1 = loop1.header
//    val header2 = loop2.header
//    header1.stmts.last
//    header2.stmts.last
//  }
//
//  def checkHeaderCondition(loop: Loop): Boolean = {
//    loop.header.stmts.last.ty == Bool
//  }
}
