package rclang
package Interface

import scala.io.Source
import lexer.*
import parser.RcParser
import analysis.SymScanner

import java.io.{PrintWriter, File}

object Compile {
  // todo:should not in interface
  def apply(option: CompileOption): Unit = {
    val f = Source fromFile option.srcPath
    // avoid last line is end and lost last empty line
    val src = f.getLines.mkString("\n") + "\n"
    f.close()
    println(src)
    val tokens = Lexer(src) match {
      case Left(value) => throw RuntimeException(value.msg)
      case Right(value) => value
    }
    println("Lexer Finish")
    // todo:dump tokens
    dumpTokens(tokens)
    val ast = RcParser(tokens) match {
      case Left(value) => throw RuntimeException(value.msg)
      case Right(value) => value
    }
    println("Parser Finish")
    // todo:dump ast
    println(ast)
    var table = SymScanner(ast)
  }

  def dumpTokens(tokens: List[Token]) = {
    val str = tokens.mkString(" ")
    val f = new PrintWriter(new File("tokens.txt"));
    f.write(str)
    f.close()
  }
}
