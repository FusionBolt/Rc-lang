package rclang
package pass

case class PassManager[IRUnitT]() {
  type CallbackT = (IRUnitT, Transform[IRUnitT]) => Unit

  def run(IRUnit: IRUnitT, am: AnalysisManager[IRUnitT]) = {
    passes.foreach(pass => {
      pass.run(IRUnit, am)
      callbacksAfterPass.foreach(f => f(IRUnit, pass))
    })
  }

  def addPass(pass: Transform[IRUnitT]): Unit = {
    passes = passes :+ pass
  }

  def registerAfterPass(callback: CallbackT) = {
    callbacksAfterPass = callbacksAfterPass :+ callback
  }

  var passes = List[Transform[IRUnitT]]()
  var callbacksAfterPass = List[CallbackT]()
}
