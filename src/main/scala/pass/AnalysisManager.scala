package rclang
package pass

import analysis.*
import mir.DomTree
import scala.language.implicitConversions

case class AnalysisManager[IRUnitT]() {
  def getResult[AnalysisT <: Analysis[IRUnitT]](IRUnit: IRUnitT)(using analysis: AnalysisT): analysis.ResultT = {
    analysis.run(IRUnit, this)
  }

  def addAnalysis(analysis: => Analysis[IRUnitT]): Unit = {
    val name = analysis.getClass.getTypeName
    if (!analyses.contains(name)) {
      analyses += (name -> analysis)
    }
  }

  var analyses = Map[String, Analysis[IRUnitT]]()
}

def getAnalysisResult[IRUnitT, AnalysisT <: Analysis[IRUnitT]](IRUnit: IRUnitT)(using analysis: AnalysisT): analysis.ResultT = {
  val am = AnalysisManager[IRUnitT]()
  am.addAnalysis(analysis)
  am.getResult(IRUnit)
}