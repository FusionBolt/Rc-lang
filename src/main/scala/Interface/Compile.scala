package rclang
package Interface

import scala.io.Source
import lexer.*
import parser.RcParser
import analysis.{BasicAA, DomTreeAnalysis, SymScanner}

import scala.language.implicitConversions
import mir.*
import tools.{DumpManager, RcLogger}
import ty.{Infer, TyCtxt, TypeCheck, TypedTranslator}
import tools.RcLogger.log
import analysis.Analysis.given
import pass.AnalysisManager

import java.io.{File, PrintWriter}
import scala.quoted.*

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
    log("typedModule.txt", _.write(typedModule.toString))
    TypeCheck(typedModule)
    val mirMod = log(ToMIR(table).proc(typedModule), "ToMIR")
    val main = mirMod.fnTable.values.head
    val begin = main.bbs.find(_.name == "0").get
    val tBr = main.bbs.find(_.name == "1").get
    val end = main.bbs.find(_.name == "3").get
    val dumpStr = traverseInst(main.instructions).mkString("\n")
    //    main.instructions.map(_.toString).mkString("\n"))
    log("mir.txt", _.write(dumpStr))
    rendFn(main, "main.dot", "RcDump")
    val tree = DomTreeBuilder().build(main)
    val n1 = tree.node(begin)
    val n2 = tree.node(end)
    val n3 = tree.node(tBr)
    val isDom = tree.isDom(n1, n2)
    var am = AnalysisManager[Function]()
    am.addAnalysis(DomTreeAnalysis())
    am.addAnalysis(BasicAA())
    var domTree = am.getResult[DomTreeAnalysis](main)
    var aa = am.getResult[BasicAA](main)
    println(domTree)
    println(tree.isDom(n3, n2))
    log("domTree.txt", _.write(tree.toString))
  }
}
