package rclang
package mir

import tools.tap
import util.chaining.scalaUtilChainingOps

case class DomTreeNode(var parentTree: DomTree, var basicBlock: BasicBlock) {
  var domChildren: List[DomTreeNode] = List()
  var iDom: DomTreeNode = null

  def addChild(child: DomTreeNode) = {
    domChildren = child :: domChildren
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
//  def build(f: Function): DomTree = {
//    val dt = DomTree(f)
//    dfs(f.entry, dt, f)
//    dt
//  }

  type Node = BasicBlock
  type DomInfoType = Map[Node, Set[Node]]
  def compute(fn: Function): DomInfoType = {
    val predMap = predecessorsMap(fn.bbs)
    val n = fn.bbs
    compute(n.toSet, predMap, fn.entry)
  }

  def compute(N: Set[Node], pred: DomInfoType, root: Node): DomInfoType = {
    var change = false;
    var T = Set[Node]()
    var D = Set[Node]()
    var Domin = Map[Node, Set[Node]]()
//    Domin(root) = Set[Node](root)
    Domin = Domin.updated(root, Set[Node](root))
    (N - root).foreach(n => {
//      Domin(n) = N
      Domin = Domin.updated(n, N)
    })

    while(!change) {
      (N - root).foreach(n => {
        T = N
        pred(n).foreach( p => {
          T = T & Domin(p)
        })
        D = T + n
        if(D != Domin(n)) then {
          change = true
//          Domin(n) = D
          Domin = Domin.updated(n, D)
        }
      })
    }
    Domin
  }
}
