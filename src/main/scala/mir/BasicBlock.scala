package rclang
package mir

class BasicBlock(var stmts: List[Instruction]) extends Value with InFunction {
  def this() = this(List())

  def terminator: Terminator = stmts.last.asInstanceOf[Terminator]

  def insert[T <: Instruction](i: T): T = {
    stmts = stmts :+ i
    i
  }
}


case class Function(var name: String,
                    var argument: List[Argument],
                    var bbs: List[BasicBlock] = List(),
                    var entryBB: BasicBlock = BasicBlock(List())) {
  def instructions = bbs.flatMap(_.stmts)
}

case class Module(var functions: Function)