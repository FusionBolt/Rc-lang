package rclang
package mir
import ty.Type

case class BasicBlock(nameStr: String, var stmts: List[Instruction] = List()) extends Value with InFunction {
  name = nameStr
  def terminator: Terminator = stmts.last.asInstanceOf[Terminator]

  def insert[T <: Instruction](i: T): T = {
    stmts = stmts :+ i
    i
  }

  def successors = terminator.successors
}

case class Function(fnName: String,
                    var argument: List[Argument],
                    var bbs: List[BasicBlock] = List()) extends GlobalValue {
  name = fnName
  var entry: BasicBlock = null
  def instructions = bbs.flatMap(_.stmts)
}

case class Module(var name: String = "", var fnTable: Map[String, Function] = Map()) {
  var globalVariables: List[GlobalVariable] = List()
  var types: Set[Type] = Set()
  var context: RcContext = null
}

case class RcContext() {
  var modules: List[Module] = List()
}