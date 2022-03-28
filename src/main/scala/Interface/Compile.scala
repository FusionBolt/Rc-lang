package rclang
package Interface

import scala.io.Source
import lexer.Lexer
import parser.RcParser

object Compile {
  def apply(option: CompileOption): Unit = {
    val f = Source fromFile option.srcPath
    val src = f.getLines.mkString("\n")
    println(src)
    val tokens = Lexer(src) match {
      case Left(value) => throw RuntimeException(value.msg)
      case Right(value) => value
    }
    println("Lexer Finish")
    // todo:dump tokens
    println(tokens)
    val ast = RcParser(tokens)
    println("Parser Finish")
    // todo:dump ast
    println(ast)
    f.close()
  }
}
