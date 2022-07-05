package rclang
package Interface

import scala.io.Source
import lexer.*
import parser.RcParser
import analysis.SymScanner

import rclang.mir.*
import rclang.tools.{DumpManager, RcLogger}
import rclang.ty.{Infer, TyCtxt, TypeCheck, TypedTranslator}
import tools.RcLogger.log

import java.io.{File, PrintWriter}


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
    val tokens = log(run(Lexer(src)), "Lexer")
    log("token.txt", _.write(tokens.mkString(" ").replace("EOL", "\n")))
    val ast = log(run(RcParser(tokens)), "Parser")
    // todo:refactor dump
    log("ast.txt", _.write(ast.toString))
    val table = SymScanner(ast).methodTypeTable.toMap
    val tyCtxt = TyCtxt(table.map((id, item) => id -> Infer(item)))
    val typedModule = TypedTranslator(tyCtxt)(ast)
    TypeCheck(typedModule)
    val funList = log(ToMIR(table).proc(typedModule), "ToMIR")
    val main = funList(0)
    rendDot(main, "main.dot", "RcDump")
    log("mir.txt", _.write(main.instructions.map(_.toString).mkString("\n")))
//    log("domTree.txt", _.write(DomTreeBuilder().build(main).toString))
  }
}
