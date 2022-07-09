package rclang
package pass

case class PassManager[IRUnitT]() {
  def run() = {
    passes.foreach(println)
  }

  def addPass(pass: Transform[IRUnitT]): Unit = {
    passes = passes :+ pass
  }

  var passes = List[Transform[IRUnitT]]()
}
