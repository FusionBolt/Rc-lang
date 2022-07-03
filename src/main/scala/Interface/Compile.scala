package rclang
package Interface

import scala.io.Source
import lexer.*
import parser.RcParser
import analysis.SymScanner

import rclang.mir.*
import rclang.tools.{DumpManager, RcLogger}
import rclang.ty.{Infer, TyCtxt, TypeCheck, TypedTranslator}

import java.io.{File, PrintWriter}

class Ex

def run[TL, TR](result: => Either[TL, TR]): TR = {
  result match {
    case Left(l) => throw new RuntimeException(l.toString)
    case Right(r) => r
  }
}
object Compile {
  def getSrc(path: String) = {
    val f = Source fromFile path
    // avoid last line is end and lost last empty line
    val src = f.getLines.mkString("\n") + "\n"
    f.close()
    src
  }
  // todo:should not in interface
  def apply(option: CompileOption): Unit = {
    DumpManager.mkDumpRootDir
    val src = getSrc(option.srcPath)
    val tokens = RcLogger.log(run(Lexer(src)), "Lexer")
    RcLogger.log("token.txt", _.write(tokens.mkString(" ")))
    val ast = RcLogger.log(run(RcParser(tokens)), "Parser")
    // todo:refactor dump
    RcLogger.log("ast.txt", _.write(ast.toString))
    val table = SymScanner(ast).methodTypeTable.toMap
    val tyCtxt = TyCtxt(table.map((id, item) => id -> Infer(item)))
    val typedModule = TypedTranslator(tyCtxt)(ast)
    TypeCheck(typedModule)
    val funList = RcLogger.log(ToMIR(table).proc(typedModule), "ToMIR")
    RcLogger.log("mir.txt", _.write(funList(0).instructions.map(_.toString).mkString("\n")))
  }
}
