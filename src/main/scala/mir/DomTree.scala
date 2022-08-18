package rclang
package mir

import tools.tap

import scala.collection.mutable.LinkedHashSet
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

  override def toString: String = s"bb:${basicBlock.name} -> ${children.map(_.basicBlock.toString).mkString(" ")}"
}

// todo:enum Entry: normal and Post
object DomEntry extends DomTreeNode(null, null) {
}

case class DomTree(var parent: Function) {
  var nodes = Map[BasicBlock, DomTreeNode]()
  def entry = nodes(parent.entry)
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
    "DomTree:\n" + nodes.values.toList.sortBy(_.name).map(d => s"${d.name} -> ${d.children.map(_.name).sorted.mkString(",")}").mkString("\n")
  }

  def serialize: List[DomTreeNode] = dfsBasicBlocks(parent.entry).map(b => nodes(b))
  
  def visit[T](f: DomTreeNode => T): List[T] = {
    serialize.map(f)
  }
}


extension (i: DomTreeNode) {
  def dom(a: DomTreeNode): Boolean = {
    // todo:反了??
    a.children.contains(i)
//    i.children.contains(a)
  }

  def sdom(a: DomTreeNode): Boolean = i != a && (i dom a)

  def idom(a: DomTreeNode): Boolean = i.iDom == a
}

def allReach(a: BasicBlock, b: BasicBlock): Boolean = {
  if (a == b) return true
  a.successors.forall(canReach(_, b))
}

type Node = BasicBlock
type DomInfoType = Map[Node, LinkedHashSet[Node]]

case class DomTreeBuilder() {
  var visited = LinkedHashSet[BasicBlock]()

  def compute(fn: Function): DomTree = {
    val predMap = predecessorsMap(fn.bbs)
    val bbs = dfsBasicBlocks(fn.entry)
    compute(LinkedHashSet.from(bbs), predMap, fn.entry)
  }

  var dumpCompute = true
  def computeLog(str: String) = {
    if (dumpCompute)
      println(str)
  }

  def computeImpl(nodes: LinkedHashSet[Node], pred: DomInfoType, root: Node): DomInfoType = {
    var change = false;
    var Domin = Map(root -> LinkedHashSet[Node](root))
    val N = nodes
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
        println("")
      })
    }
    Domin
  }

  def compute(nodes: LinkedHashSet[Node], pred: DomInfoType, root: Node): DomTree = {
    val Domin = computeImpl(nodes, pred, root)
    makeTree(Domin, root.parent)
  }

  def makeTree(Domin: DomInfoType, f: Function) : DomTree = {
    val tree = DomTree(f)
    Domin.foreach(d => {
      tree.addNode(d._1)
    })
    Domin.foreach(d => {
      tree.node(d._1).addChilds(d._2.map(tree.node).toList)
    })
    tree
  }
}

def idomComputeLog(str: String) = {
  if (false)
    println(str)
}

def iDomCompute(N: LinkedHashSet[Node], Domin: DomInfoType, root: Node): Map[Node, Node] = {
  var tmp = N.foldLeft(Map[Node, LinkedHashSet[Node]]())((acc, n) =>
    acc.updated(n, Domin(n) - n)
  )

  //  a != b && a dom b && not exist c: a dom c && c dom b

  // all dominators
  (N - root).toList.sortBy(_.name).foreach(a => {

    idomComputeLog("a: " + a.name)
    idomComputeLog("tmp: " + tmp(a).map(_.name).mkString(", "))
    // tmp(n) - n ==> a dom b && a != b
    (tmp(a) - a).foreach(b => {
      // node c ==> a dom c
      // c != b
      idomComputeLog("b: " + b.name)
      (tmp(a) - b).foreach(c => {
        // if c dom b, then is not idom, should remove from tmp
        if(tmp(c).contains(b)) {
          idomComputeLog("c: " + c.name)
          idomComputeLog("reduce: " + b.name)
          tmp = tmp.updated(a, tmp(a) - b)
        }
      })
    })
    idomComputeLog("")
  })

  (N - root).map(n => {
    println(n.name)
    println(tmp(n))
    (n -> tmp(n).head)
  }).toMap
}