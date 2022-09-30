package rclang
package opt

import pass.{AnalysisManager, Transform}
import mir.{Alloc, Binary, BinaryInstBase, Branch, Call, CondBranch, Function, GetElementPtr, Intrinsic, Load, MultiSuccessorsInst, PhiNode, Return, Store, SwitchInst, UnaryInst}
import ty.*

class SROA extends Transform[Function] {
  def findAggregates(f: Function): List[Call] = {
    f.instructions.filter(_ match
      case Call(f) => structTyProc(f._1.ty, false)(true)
        case _ => false
    )
  }

  override def run(iRUnitT: Function, AM: AnalysisManager[Function]): Unit = {
    val aggregates = findAggregates(iRUnitT)

  }
}
