package rclang
package integrated

import org.scalatest.Outcome
import rclang.compiler.Driver

import java.io.File

def listFiles(dir: File): List[File] = {
  val files = dir.listFiles.toList
  files.flatMap {
    case file if file.isDirectory => listFiles(file)
    case file => List(file)
  }.filter(f => f.getName.split('.')(1) == "rc")
}

class DemoCompileTest extends RcTestBase {
  val dir = new File(new File("").getAbsolutePath, "demo")
  val files = listFiles(dir).filter(f => !(f.getPath.contains("template") || f.getPath.contains("lib")))
  describe("run all") {
    it("ok") {
      var error = List[String]()
      files.foreach(file => {
        try {
          val ast = Driver.parse(file.getPath)
          Driver.compileAST(ast)
        } catch {
          case _: Throwable => {
            error = error.appended(file.getPath)
          }
        }
      })
      if(error.nonEmpty) {
        error.foreach(println)
        assert(false)
      }
    }
  }
}
