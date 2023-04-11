package rclang
package transform

import pass.{AnalysisManager, Transform}
import mir.*

class CSE extends Transform[Function] {
  var exprSet = Map[(String, Value, Value), Value]()
  // load ptr -> load inst
  var avaliableLoads = Map[Value, Value]();
  override def run(iRUnitT: Function, AM: AnalysisManager[Function]): Unit = {
    var workList = List[Instruction]();
    iRUnitT.instructions.foreach { inst => inst match
      case Load(ptr) => {
        avaliableLoads.get(ptr) match
          case Some(value) => {
            inst.replaceAllUseWith(value)
            workList = inst :: workList
          }
          case None => {
            avaliableLoads = avaliableLoads.updated(ptr, inst)
          }
      }
      case bn @ Binary(op, lhs, rhs) => {
        val key = (op, lhs, rhs)
        exprSet.get(key) match
          case Some(value) => {
            inst.replaceAllUseWith(exprSet(key))
            workList = inst :: workList
          }
          case None => {
            exprSet = exprSet.updated(key, inst)
          }
      }
      case _ =>
    }

//    print(exprSet)

    workList.foreach(_.eraseFromParent)
  }
}
