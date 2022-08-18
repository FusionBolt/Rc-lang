package rclang
package pass

case class PassManager[IRUnitT]() {
  def run(IRUnit: IRUnitT, am: AnalysisManager[IRUnitT]) = {
    passes.foreach(_.run(IRUnit, am))
  }

  def addPass(pass: Transform[IRUnitT]): Unit = {
    passes = passes :+ pass
  }

  var passes = List[Transform[IRUnitT]]()
}
