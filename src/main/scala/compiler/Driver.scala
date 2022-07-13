package rclang
package compiler

import analysis.{BasicAA, DomTreeAnalysis, SymScanner}
import lexer.Lexer
import mir.*
import parser.RcParser
import pass.AnalysisManager
import tools.*
import ty.{Infer, TyCtxt, TypeCheck, TypedTranslator}
import tools.RcLogger.log
import analysis.Analysis.given

import scala.io.Source

object Driver {
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
    val tokens = log(Lexer(src).unwrap, "Lexer")
    log("token.txt", _.write(tokens.mkString(" ").replace("EOL", "\n")))
    val ast = log(RcParser(tokens).unwrap, "Parser")
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
    println("reach: " + allReach(begin, end))
    println("reach: " + allReach(tBr, end))
    val tree = DomTreeBuilder().build(main)
    println(tree.nodes.keys.map(_.name).mkString(","))

    val n1 = tree.node(begin)
    val n2 = tree.node(end)
    val d = n1 dom n2

    println(tree.nodes.contains(tBr))

    val r = n1 dom n2
    val id = sdom(n1, n2)
    println("is Dom: " + d)
    println("is IDom: " + id)
    var am = AnalysisManager[Function]()
    am.addAnalysis(DomTreeAnalysis())
    am.addAnalysis(BasicAA())
    var domTree = am.getResult[DomTreeAnalysis](main)
    var aa = am.getResult[BasicAA](main)
    log("domTree.txt", _.write(tree.toString))
  }
}
