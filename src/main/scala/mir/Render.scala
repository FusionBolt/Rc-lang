package rclang
package mir

import tools.Render

class CFGRender extends Render {
//  var blocksEntryName = "blocksEntry"
  def BBEdges(dot: Digraph, bb: BasicBlock) = {
    val name = bb.name
    dot.node(name)
    dot.edges(bb.terminator.successors.map(b => (name, b.name)).toArray)
  }

  def rendBBs(fileName: String, directory: String, bbs: List[BasicBlock]): Unit = {
    rend(fileName, directory, bbs)(BBEdges)
  }

  def rendFn(fileName: String, directory: String, fn: Function): Unit = {
//    blocksEntryName = fn.entry.name
    rendBBs(fileName, directory, fn.bbs)
  }

  override def rendInit(dot: Digraph): Unit = {
//    dot.node("entry", null, collection.mutable.Map("URL" -> "\"https://www.google.com\""))
//    dot.edge("entry", blocksEntryName)
  }
}