package rclang
package mir

def rendFn(f: Function, fileName: String, directory: String): Unit = {
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