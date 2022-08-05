package rclang
package codegen

trait Section

val indent = "  "
case class TextSection(var fns: Map[String, List[String]] = Map()) extends Section {
  override def toString: String = {
    val decls = "section .text\n" + fns.keys.map(n => s"${indent}.globl $n\n${indent}.type  $n, @function").mkString + "\n"
    val fnInst = fns.map((name, inst) => s"$name\n${inst.map(indent + _).mkString("\n")}").mkString("\n")
    decls + fnInst
  }

  def addFn(fn: (String, List[String])) = {
    // todo:refactor
    fns = fns.updated(fn._1, fn._2)
  }
}

case class Assembly() {
  def file: String = ""
  var dataSection: List[String] = List()
  var textSection: List[String] = List()
  var bssSection: List[String] = List()
//  def global: Unit
//  def label: Unit
//  def comment: Unit

  def serialize: String = {
    s".file \"${file}\"\n" + textSection + dataSection
  }

  def dataSec: String = {
    sectionSerialize("data", dataSection)
  }

  def textSec: String = {
    sectionSerialize("section", textSection)
  }

  def sectionSerialize(name: String, section: List[String]): String = {
    s"section .${name}\n" + section.map(indent + _).mkString("\n") + "\n"
  }
}
