package rclang
package codegen

class RegisterAllocManager(val regLimit: Int) {
  // n个reg对应n个operand
  var regs: Seq[Option[MachineOperand]] = Range.Int(0, regLimit, 1).map(_ => None)

  def alloc(ops: List[MachineOperand]) = {
    emptyIndexList.take(ops.size).zip(ops).foreach((index, op) => {
      regs = regs.updated(index, Some(op))
    })
  }

  def emptyIndexList = regs.zipWithIndex.filter((reg, _) => reg.isEmpty).map((_, i) => i)

  def emptyCount = emptyIndexList.size

  def releaseUnused() = {
  }

  def hasAlloc(operand: MachineOperand) = regs.contains(Some(operand))
}


class RegisterAllocation {
  val regLimit = 4
  val allocator = RegisterAllocManager(regLimit)

  // 按照顺序入队列，每个值有自己的寄存器

  //  val regMap = Map[MachineFunction, Int]()
  def run(mf: MachineFunction): Unit = {
    val list = mf.instructions.map(inst => inst.operands)
    // todo: 考虑分支的情况
    list.foreach((operand) => {
      // 找到当前列表中结束生命周期的位置释放掉
      allocator.releaseUnused()
      val ops = getNeedSize(operand)
      val needSize = ops.size
      // 1. 如果在limit之内则顺序分配
      if (allocator.emptyCount > needSize) {
        allocator.alloc(ops)
      } else {
        // 2. 否则就挑选几个置换到内存中，下次使用需要再load进来
        val lostSize = needSize - allocator.emptyCount
        ???

      }
    })
  }

  def getNeedSize(list: List[MachineOperand]) = {
    // 统计需要分配的数量，即对应参数不在寄存器中的数量
    list.filter(allocator.hasAlloc)
  }
}

class StackRegisterAllocation {
  def run(mf: MachineFunction): Unit = {
    val frameInfo = mf.frameInfo
    val allVReg = mf.instructions.flatMap(m => m.operands).map(_ match
      case v: VReg => Some(v)
      case _ => None).filter(_.isDefined).map(_.get)
    allVReg.foreach(reg => {
      // 2. update FrameInfo
      val item = frameInfo.addItem(TmpItem(4))
      // 3. replace operand
      reg.replaceFromParent(FrameIndex(item.offset))
    })
  }
}