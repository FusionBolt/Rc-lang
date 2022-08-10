package rclang
package compiler

import analysis.{BasicAA, SymScanner}
import lexer.Lexer
import mir.*
import parser.RcParser
import pass.AnalysisManager
import tools.*
import ast.ClassesRender
import ty.{Infer, TyCtxt, Type, TypeCheck, TypedTranslator}
import tools.RcLogger.{log, logf}
//import analysis.Analysis.`given`
import ast.{Class, Ident, Item, RcModule}
import codegen.{GNUAssembler, GNUASM, RDataSection, StrSection, TextSection, toLIR}

import scala.sys.process.*
import scala.io.Source
import java.io.File
import java.nio.file.Path

object Driver {
  def getSrc(path: String) = {
    val f = Source fromFile path
    // avoid last line is end and lost last empty line
    val src = f.getLines.mkString("\n") + "\n"
    f.close()
    src
  }

  def parse(src: String): RcModule = {
    val tokens = log(Lexer(src).unwrap, "Lexer")
    logf("token.txt", tokens.mkString(" ").replace("EOL", "\n"))
    log(RcParser(tokens).unwrap, "Parser").tap {
      logf("ast.txt", _)
    }
  }

  def typeProc(ast: RcModule): (RcModule, Map[Ident, Item]) = {
    val table = SymScanner(ast)
    val tyCtxt = TyCtxt()
    tyCtxt.setGlobalTable(table)
    val typedModule = TypedTranslator(tyCtxt)(ast)
    logf("typedModule.txt", typedModule)
    ClassesRender().rendClasses("classes.dot", "RcDump", typedModule.items collect { case i: Class => i })
    TypeCheck(typedModule)
    (typedModule, tyCtxt.globalTable.methodTypeTable.toMap)
  }

  def apply(option: CompileOption): Unit = {
    DumpManager.mkDumpRootDir
    val src = getSrc(option.srcPath)
    val ast = parse(src)
    val (typedModule, table) = typeProc(ast)
    val mirMod = log(ToMIR(table).proc(typedModule), "ToMIR")
    logf("mir.txt", mirMod)
    codegen(mirMod)
  }

  def codegen(mirMod: Module) = {
    val fns = log(toLIR(mirMod), "ToLIR")
//    println(fns.name)
    val fnStr = fns.mkString("\n\n")
    logf("LIR.txt", fnStr)

    val text = TextSection()
    val strTable = fns.flatMap(fn => (0 until fn.strTable.size).zip(fn.strTable.keys).map((i, str) => StrSection(i, List(str))))
    val rdata = RDataSection(strTable)
    fns.foreach(fn => text.addFn(fn.name -> fn.instructions.map(GNUASM.toASM)))
    //  text.addFn(fns(1).name -> fns(1).instructions.map(GNUASM.toASM))
    logf("asm.s", text.asm + rdata.asm)
    val o = log(as(DumpManager.getDumpRoot /"asm.s", DumpManager.getDumpRoot / "tmp.o"), "As")
//    log(toELF(o.get), "ToELF")
  }

  def as(srcPath: String, destPath: String): Option[String] = {
    val args = List(srcPath, "-o", destPath)
    val out = s"as ${args.mkString(" ")}".!!
    Some(destPath)
  }

  def toELF(asmPath: String) = {
    val outPath = asmPath.replace("tmp.o", "a.out")
    val args = List(asmPath, "-o", outPath)
    val out = s"gcc ${args.mkString(" ")}".!!
  }

  extension (dir: String) {
    def /(file: String): String = {
      s"$dir${File.separator}$file"
    }
  }
}
