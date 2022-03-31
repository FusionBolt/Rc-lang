package rclang
package Interface

import scala.io.Source
import lexer.{Lexer, Token}
import parser.RcParser

import java.io.{PrintWriter, File}

object Compile {
  def apply(option: CompileOption): Unit = {
    val f = Source fromFile option.srcPath
    // avoid last line is end and lost last empty line
    val src = f.getLines.mkString("\n") + "\n"
    println(src)
    val tokens = Lexer(src) match {
      case Left(value) => throw RuntimeException(value.msg)
      case Right(value) => value
    }
    println("Lexer Finish")
    // todo:dump tokens
    dumpTokens(tokens)
    val ast = RcParser(tokens)
    println("Parser Finish")
    // todo:dump ast
    println(ast)
    f.close()
  }

  def dumpTokens(tokens: List[Token]) = {
    val str = tokens.mkString(" ")
    val f = new PrintWriter(new File("tokens.txt"));
    f.write(str)
    f.close()
  }
}
