package rclang
package transform

import pass.{AnalysisManager, Transform}
import mir.*

import rclang.analysis.{DomFrontierAnalysis, DomTreeAnalysis}
import rclang.analysis.Analysis.{given_DomFrontierAnalysis, given_DomTreeAnalysis}

import scala.language.postfixOps

class SSAConstruct extends Transform[Function] {
  override def run(iRUnitT: Function, AM: AnalysisManager[Function]): Unit = {
    // 1. put var def in begin
    val df = AM.getResult[DomFrontierAnalysis](iRUnitT)
    val dom = AM.getResult[DomTreeAnalysis](iRUnitT)
    val locals = getNames(iRUnitT)
    locals.foreach(v => {
      var frontier = Set[BasicBlock]()
      var workList = Set[BasicBlock]()

      getDefs(v).foreach(defInst => {
        workList = workList + defInst.parent
      })

      while(workList.nonEmpty) {
        val x = workList.head
        workList = workList - x
        df(dom(x)).map(_.basicBlock).foreach(y => {
          if(!frontier.contains(y)) {
            y.stmts = List(PhiNode(Map())):::y.stmts
            frontier = frontier + y
            if (!getDefs(v).contains(y)) {
              workList = workList + y
            }
          }
        })
      }
    })

    locals.foreach(v => {
      v.reachingDef = {}
    })

    dfsDomTree(dom).map(_.basicBlock).foreach(bb => {
      bb.stmts.foreach(i => {
        // used by non phi
        i.operands.foreach(v => {
          updateReachingDef(v, i)
        })
        // used by maybe phi
        // e.g. i = phi(i, k)
        i.operands.foreach(v => {
          updateReachingDef(v, i)
        })
      })

      // todo: direct successor of BB?
      bb.successors.flatMap(bb => bb.stmts).foreach(phi => {
      })
    })
  }

  def dfsDomTree(root: DomTree): List[DomTreeNode] = ???

  def getNames(f: Function): List[Alloc] = ???

  def getDefs(alloc: Alloc): List[Instruction] = ???

  def updateReachingDef(v: Alloc, i: Instruction) = ???

  def containsPhi(bb: BasicBlock) = bb.stmts.contains {
    case PhiNode => true
    case _ => false
  }
}
