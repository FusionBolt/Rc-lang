package rclang
package mir

import tools.tap
import util.chaining.scalaUtilChainingOps

case class DomTreeNode(var parentTree: DomTree, var basicBlock: BasicBlock) {
  var domChildren: List[DomTreeNode] = List()
  var iDom: DomTreeNode = null

  def addChild(child: DomTreeNode) = {
    domChildren = child :: domChildren
    child.iDom = this
  }
}

// todo:enum Entry: normal and Post
object DomEntry extends DomTreeNode(null, null) {
}

case class DomTree(var parent: Function) {
  var nodes = Map[BasicBlock, DomTreeNode]()
  var entry = DomEntry
  entry.parentTree = this
  entry.domChildren = List(addNode(parent.entry))

  // todo:finish, should recalc
  def addNode(bb: BasicBlock): DomTreeNode = {
    DomTreeNode(this, bb).tap(node =>
      nodes += (bb -> node)
    )
  }

  def node(bb: BasicBlock): DomTreeNode = nodes(bb)
}

extension (i: DomTreeNode) def dom(a: DomTreeNode): Boolean = {
  if(i == a) {
    true
  } else {
    i.domChildren.find(child => child dom a).isDefined
  }
}

infix def sdom(i: DomTreeNode, a: DomTreeNode): Boolean = i != a && (i dom a)

// todo: infix format? i iDom b
infix def idom(i: DomTreeNode, a: DomTreeNode): Boolean = i.iDom == a

def allReach(a: BasicBlock, b: BasicBlock): Boolean = {
  if (a == b) return true
  a.successors.forall(canReach(_, b))
}

case class DomTreeBuilder() {
  var visited = Set[BasicBlock]()
  def build(f: Function): DomTree = {
    val dt = DomTree(f)
    dfs(f.entry, dt, f)
    dt
  }

  private def dfs(bb: BasicBlock, tree: DomTree, f: Function): Option[DomTreeNode] = {
    // strict dominance
    if (visited.contains(bb)) {
      return None
    }
    val prevBB = bb.prev

    if (prevBB.successors.size == 1) {
      // prevBB
      //   |
      //  bb
      val node = tree.addNode(bb)
      tree.node(prevBB).addChild(node)
      // todo:fix this
      Some(node)
      // visit bb's child
    } else {

      // other prevBB
      //   \   /
      //     b
      prevBB.successors.foreach(succ => {
        val node = tree.addNode(succ)
        tree.node(prevBB).addChild(node)
      })
      val childs = bb.terminator.successors.map(next => {
        dfs(next, tree, f)
      })
      if (allReach(tree.entry.domChildren.head.basicBlock, bb)) {
        val node = tree.addNode(bb)
        node.domChildren = childs.flatten
        Some(node)
      } else {
        None
      }
    }
  }
}
