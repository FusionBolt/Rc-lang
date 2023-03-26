package rclang

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.should.Matchers

import java.io.File
import tools.{DumpManager, RcLogger}
import tools.RcLogger.{log, logf}
import compiler.Driver.*
import rclang.mir.ToMIR
import tools./

class RcTestBase extends AnyFunSpec with BeforeAndAfter with Matchers {
  DumpManager.setDumpRoot("RcTestDump")

  def getModule(src: String) = {
    val ast = parse(src)
    val (typedModule, table) = typeProc(ast)
    val mirMod = log(ToMIR(table).proc(typedModule), "ToMIR")
    mirMod
  }

  def getFirstFn(srcPath: String) = {
    val src = getSrc(srcPath)
    val mirMod = getModule(src)
    mirMod.fnTable.values.head
  }

  def getDemoFirstFn(name: String) = {
    getFirstFn("demo" / name)
  }
  
  def getOptDemoFirstFn(name: String) = {
    getFirstFn("demo" / "opt" / name)
  }
}
