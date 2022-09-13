package rclang
package analysis

import mir.*
import pass.{Analysis, AnalysisManager}

case class AAResult() {

}

case class BasicAA() extends Analysis[Function] {
  type ResultT = AAResult
  def run(irUnit: Function, AM: AnalysisManager[Function]): ResultT = {
    AAResult()
  }
}
