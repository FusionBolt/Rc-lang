package rclang

import Interface.*
import compiler.CompileOption

import scala.util.CommandLineParser
import scopt.OParser

given CommandLineParser.FromString[CompileOption] with {
  override def fromString(s: String): CompileOption = {
    val builder = OParser.builder[CompileOption]
    val parser = {
      import builder._
      OParser.sequence(
        programName("rcc"),
        head("rcc", "0.0.0"),
        arg[String]("<file>...")
          .unbounded()
          .action((x, c) => c.copy(srcPath = List(x):::c.srcPath))
          .text("optional unbounded args"),
        opt[String]('o', "output")
          .text("set output file name"),
        opt[Unit]('t', "target")
          .text("show available target")
          .action((_, c) => {
            println("only suppport GNU ASM")
            c
          }),
        help('h', "help").text("display available options"),
      )
    }
    OParser.parse(parser, s.split(' '), CompileOption()) match
      case Some(value) => value
      case None => CompileOption()
  }
}

@main def rcc(option: CompileOption) = compiler.Driver(option)