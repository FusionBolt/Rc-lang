package rclang
package Interface

import scala.io.Source
import lexer.Lexer
import parser.RcParser

object Compile {
  def apply(option: CompileOption): Unit = {
    val f = Source fromFile option.srcPath
    val src = f.getLines.mkString
    println(src)
    val ast = RcParser(Lexer(src).getOrElse(List()))
    println(ast)
    f.close()
  }
}
