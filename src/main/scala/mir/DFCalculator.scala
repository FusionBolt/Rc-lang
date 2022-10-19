package rclang
package mir

case class DFCalculator(var domTree: DomTree) {
  def findParents(tree: DomTree, root: DomTreeNode): Set[DomTreeNode] = {
//    println(s"root:${root.name}")
    val result = domTree.nodes.filter(node => {
      if (node._2.iDom == null) {
        false
      } else {
//        println(s"node:${node._1.name} -> ${node._2.iDom.name}")
        root idom node._2
      }
    }).values.toSet
//    println(s"root:${root.name} end")
    result
  }

  def run(bb: BasicBlock): List[BasicBlock] = {
    val parents = findParents(domTree, domTree(bb))
    val result = parents.map(node => findParents(domTree, node)).reduce(_ | _)
    result.map(_.basicBlock).toList
  }
}
