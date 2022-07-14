package rclang
package mir

def rendDot(f: Function, fileName: String, directory: String): Unit = {
  rendDotImpl(fileName, directory) { dot =>
    dot.node("entry")
    dot.edge("entry", f.entry.name)
    addBBEdges(f.bbs, dot)
  }
}

def rendDot(bbs: List[BasicBlock], fileName: String, directory: String): Unit = {
  rendDotImpl(fileName, directory) { dot =>
    addBBEdges(bbs, dot)
  }
}

private def addBBEdges(bbs: List[BasicBlock], dot: Digraph): Unit = {
  bbs.foreach(bb => {
    val name = bb.name
    dot.node(name)
    dot.edges(bb.terminator.successors.map(b => (name, b.name)).toArray)
  })
}

private def rendDotImpl(fileName: String, directory: String)(f: Digraph => Unit): Unit = {
  val dot = new Digraph()
  f(dot)
  dot.render(fileName = fileName, directory = directory, format = "svg")
}