package rclang
package pass

trait Pass[IRUnitT] {
}

trait Transform[IRUnitT] extends Pass[IRUnitT] {
  def run(iRUnitT: IRUnitT, AM: AnalysisManager[IRUnitT]): Unit
}

trait Analysis[IRUnitT] extends Pass[IRUnitT] {
  type ResultT
  def run(irUnit: IRUnitT, AM: AnalysisManager[IRUnitT]): ResultT
}