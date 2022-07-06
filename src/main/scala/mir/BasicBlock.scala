package rclang
package mir


case class BasicBlock(var name: String, var stmts: List[Instruction] = List()) extends Value with InFunction {
  def terminator: Terminator = stmts.last.asInstanceOf[Terminator]

  def insert[T <: Instruction](i: T): T = {
    stmts = stmts :+ i
    i
  }
}

case class Function(var name: String,
                    var argument: List[Argument],
                    var bbs: List[BasicBlock] = List()) {
  var entry: BasicBlock = null
  def instructions = bbs.flatMap(_.stmts)
}

case class Module(var functions: Function)

def rendDot(f: Function, fileName: String, directory: String): Unit = {
  val dot = new Digraph()
  dot.node("entry")
  dot.edge("entry", f.entry.name)
  f.bbs.foreach(bb => {
    val name = bb.name
    dot.node(name)
    dot.edges(bb.terminator.successors.map(b => (name, b.name)).toArray)
  })
  dot.render(fileName = fileName, directory = directory, format = "svg")
}