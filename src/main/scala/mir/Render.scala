package rclang
package mir

import tools.{DumpManager, Render}

object CFGRender extends Render {
//  var blocksEntryName = "blocksEntry"
  def BBEdges(dot: Digraph, bb: BasicBlock) = {
    val name = bb.name
    dot.node(name)
    dot.edges(bb.terminator.successors.map(b => (name, b.name)).toArray)
  }

  def rendBBs(bbs: List[BasicBlock], fileName: String, directory: String = DumpManager.getDumpRoot): Unit = {
    rend(fileName, directory, bbs)(BBEdges)
  }

  def rendFn(fn: Function, fileName: String, directory: String = DumpManager.getDumpRoot): Unit = {
//    blocksEntryName = fn.entry.name
    rendBBs(fn.bbs, fileName, directory)
  }

  override def rendInit(dot: Digraph): Unit = {
//    dot.node("entry", null, collection.mutable.Map("URL" -> "\"https://www.google.com\""))
//    dot.edge("entry", blocksEntryName)
  }
}