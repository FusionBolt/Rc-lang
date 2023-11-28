package rclang
package transform

import mir.*
import pass.{AnalysisManager, Transform}

// before
// bb1.
// f() = a + b * c
// val n = 1
// val d = f()
// d + n

// after
// bb1.
// val n = 1
// val tmp = a + b * c
// branch

// bb2.
// val d = tmp
// d + n

class CallInliner extends Transform[Function] {
  // 1. find call
  // 2. args copy
  // 3. return copy
  // 4. call bb split to two bb
  override def run(fn: Function, AM: AnalysisManager[Function]): Unit = {
    // todo: only process for call, invoke maybe cause exception and basicblock shoudld rebuild
    fn.instructions
      .collect { case a: Call => a }
      // todo: 如果foreach替换了后面的call，那么就不能继续从当前的开始了，这个只能支持一个bb里面一个call，因为实现会破坏bb
      .foreach(inst => {
        val f = inst.func
        if(canInline(f)) {
          // make new bb
          val newAfterBB: BasicBlock = new BasicBlock("afterBB")
          val (insts, alloc) = getInlineInsts(f, inst, newAfterBB)
          val instIndex = inst.parent.stmts.indexOf(inst)
          // inst后面的inst拷贝到newAfterBB，
          val after = inst.parent.stmts.slice(instIndex + 1, inst.parent.stmts.length)
          newAfterBB.stmts = after
          // 之后inst要被替换为insts
          inst.parent.stmts = inst.parent.stmts.slice(0, instIndex) ::: insts
          // insert bb
          fn.bbs = fn.bbs:+newAfterBB
        }
    })
  }

  def replaceArgument(insts: List[Instruction], pair: Map[Argument, Value]): List[Instruction] = {
    insts.map(inst => {
      val newOperands = inst.getOperands.map {
        case param: Argument => pair(param)
        case i => i
      }
      inst.setOperands(newOperands)
      inst
    })
  }

  def getInlineInsts(f: Function, inst: Call, newAfterBB: BasicBlock): (List[Instruction], Alloc) = {
    val originInstList = f.instructions

    // 如果最后一个不是return，是隐式返回的话，那么要在lower的过程添加return才行
    originInstList.last match
      case r: Return =>
      case i: _ => throw new RuntimeException()

    val argMap = f.argument.zip(inst.args).toMap
    val instList = replaceArgument(originInstList, argMap)

    // 保存返回值
    val alloc = new Alloc("retTmp", f.retType)
    // 简单替换所有return
    val instWithoutReturn = instList.flatMap {
      case r: Return => List(new Store(r.value, alloc), Branch(newAfterBB))
      case v => List(v)
    }

    (instWithoutReturn, alloc)
  }

  // inline for simple function, but this is a very simple strategy
  def canInline(fn: Function): Boolean = {
    fn.bbs.length <= 1 && fn.instructions.length < 10
  }
}
