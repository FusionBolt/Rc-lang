package rclang
package interpreter

import scala.io.StdIn.readLine
import ast.*
import lexer.Lexer
import tools.{RcLogger, unwrap}
import parser.{RcExprParser, RcParser}

case class Interpreter() {
  val prompt: String = "rc> "
  var evaluator = Evaluator()
  def run = {
    var isRunning = true
      while (true) {
        print(prompt)
        val line = readLine + "\n"
        if(line == null) {
          isRunning = false
        }
        else {
          if(line.nonEmpty) {
            val result = interpret(line)
            println(result)
          }
        }
      }
  }

  def interpret(str: String): Any = {
    val tokens = Lexer(str).unwrap
    println(tokens)
    val ast = RcExprParser(tokens).unwrap
    evaluator.run_stmt(ast)
  }
}