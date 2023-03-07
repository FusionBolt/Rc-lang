package rclang
package codegen

import mir.Instruction

class LiftTime {
  var data = Map[Instruction, (Int, Int)]()
}

class LifeTimeAnalysis {
  def run(mf: MachineFunction): Unit = {
    val size = mf.instructions.size
    val regMap = Array.ofDim[Int](size, size)
    // 针对每个inst的def进行分析
    mf.instructions.zipWithIndex.foreach((inst, i) => {
      // 每个inst在不同的bb中的时间
      if (inst.operands.nonEmpty) {
        regMap(i)(i) = 1
      }
      // todo: split with bb
      mf.instructions.zipWithIndex.slice(i + 1, mf.instructions.size).foreach((userInst, subI) => {
        if (userInst.useIt(inst)) {
          regMap(i)(subI) = 1
        }
      })
    })

    regMap.foreach(useByOnInst => {
      print("|")
      useByOnInst.foreach(isUse => {
        print(s"$isUse|")
      })
      println()
    })

    val range = detectRange(regMap)

    // 1. lifetime is local, then directly sub index
    // 2. 如果是跨越块了，那么在join分支里除了phi所指的之外lifetime都会结束

    // 遍历每个mbb
    // 遍历mbb的每个指令
    // 遍历指令所有的operand
    // operand找到其出现的所有位置

    // 分支的情况怎么办，比如说在左边的分支其实已经结束lifetime了，但是在右边还没有，是不是应该针对每个bb来分配

  }

  def detectRange(regMap: Array[Array[Int]]): List[List[(Int, Int)]] = {
    regMap.zipWithIndex.map((regMap, i) => detectRange(regMap, i)).toList
  }

  def detectRange(regMap: Array[Int], i: Int) = {
    // find not zero, find next no zero, update index
    var first = i
    val last = regMap.length
    var result: List[(Int, Int)] = List()
    while (first != last) {
      val begin = regMap.indexOf(1, first)
      if(begin == -1) {
        first = last
      } else {
        var end = regMap.indexOf(0, begin)
        if (end == -1) {
          end = last
        }
        result = result.appended((begin, end))
        first = end
      }
    }
    result
  }
}
