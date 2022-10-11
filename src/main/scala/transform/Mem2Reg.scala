//package rclang
//package transform
//
//import pass.{AnalysisManager, Transform}
//import mir.*
//
//// 1. find allocs
//// 2. find all load, store
//// 3. delete more store, add new alloc, rename alloc
//class Mem2Reg extends Transform[Function] {
//  def collectAlloc(inst: List[Instruction]): List[Alloc] = {
//    inst.filter{case alloc: Alloc =>
//      // alloc与store之间不能有其他的usage
//      // 找到第一个store
//      // 1. 没有第二个store则不需要promote
//    }
//  }
//
//  def promote(allocs: List[Alloc]) = {
//
//  }
//
//  override def run(iRUnitT: Function, AM: AnalysisManager[Function]): Unit = {
//    val allocs = collectAlloc(iRUnitT.instructions)
//  }
//}
