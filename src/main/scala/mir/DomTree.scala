package rclang
package mir

import tools.tap
import util.chaining.scalaUtilChainingOps

class DomTreeNode(var parentTree: DomTree, var basicBlock: BasicBlock, var children: List[DomTreeNode] = List()) {
  var iDom: DomTreeNode = null

  def name: String = basicBlock.name

  def addChild(child: DomTreeNode) = {
    children = child :: children
  }

  def addChilds(childs: List[DomTreeNode]) = {
    children = childs ::: children
  }
}

// todo:enum Entry: normal and Post
object DomEntry extends DomTreeNode(null, null) {
}

case class DomTree(var parent: Function) {
  var nodes = Map[BasicBlock, DomTreeNode]()
  var entry = DomEntry
  entry.parentTree = this
//  entry.domChildren = List(addNode(parent.entry))

  // todo:finish, should recalc
  def addNode(bb: BasicBlock): DomTreeNode = {
    DomTreeNode(this, bb).tap(node =>
      nodes += (bb -> node)
    )
  }

  def apply(bb: BasicBlock) = node(bb)
  def node(bb: BasicBlock): DomTreeNode = nodes(bb)

  override def toString: String = {
    nodes.values.toList.sortBy(_.name).map(d => s"${d.name} -> ${d.children.map(_.name).sorted.mkString(",")}").mkString("\n")
  }
}


extension (i: DomTreeNode) {
  def dom(a: DomTreeNode): Boolean = {
    i.children.contains(a)
  }

  def sdom(a: DomTreeNode): Boolean = i != a && (i dom a)
}

// todo: infix format? i iDom b
infix def idom(i: DomTreeNode, a: DomTreeNode): Boolean = i.iDom == a

def allReach(a: BasicBlock, b: BasicBlock): Boolean = {
  if (a == b) return true
  a.successors.forall(canReach(_, b))
}

case class DomTreeBuilder() {
  var visited = Set[BasicBlock]()

  type Node = BasicBlock
  type DomInfoType = Map[Node, Set[Node]]

  def compute(fn: Function): DomTree = {
    val predMap = predecessorsMap(fn.bbs)
    val bbs = dfsBasicBlocks(fn.entry)
    compute(bbs, predMap, fn.entry)
  }

  var dumpCompute = false

  def computeLog(str: String) = {
    if (dumpCompute)
      println(str)
  }

  def compute(nodes: List[Node], pred: DomInfoType, root: Node): DomTree = {
    var change = false;
    var Domin = Map(root -> Set[Node](root))
    val N = nodes.toSet
    (N - root).foreach(n => {
      Domin = Domin.updated(n, N)
    })
    assert(Domin(root).size == 1)
    assert(root != null)

    // remove entry
    val workList = nodes.tail
    while (!change) {
      workList.foreach(n => {
        computeLog(s"process: ${n.name}")
        val preds = pred(n)
        computeLog(s"preds: ${preds.map(_.name).mkString(", ")}")
        // union set of all predecessors dominator set
        // first node only be dominated by itself
        // first result of tmpDom is {root}
        val tmpDom = preds.foldLeft(N) { (acc, p) => acc & Domin(p) }
        // predecessors dom set + self (not strict dominate)
        val D = tmpDom + n
        if (D != Domin(n)) then {
          computeLog(s"Dom: ${D.map(_.name).mkString(", ")}")
          change = true
          Domin = Domin.updated(n, D)
        }
      })
    }
    val tree = DomTree(root.parent)
    Domin.foreach(d => {
      tree.addNode(d._1)
    })
    Domin.foreach(d => {
      tree.node(d._1).addChilds(d._2.map(tree.node).toList)
    })
    tree
  }
}
