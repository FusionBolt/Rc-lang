package rclang
package mir

case class DFCalculator(var domTree: DomTree) {
  def findParents(tree: DomTree, root: DomTreeNode): Set[DomTreeNode] = {
    val map = predecessorsMap(tree.nodes.keys.toList)
    val res = domTree.nodes.filter(node => {
      val pres = map(node._1)
//      println(s"current node: ${node._1.name}")
//      println(s"preds $pres")
      // join node -> pres.size > 1

      // n -> x -> y
      // x sdom y
      // n dom y
      pres.size > 1 && pres.exists(p => {
        println(s"p: ${p.name}")
        println(domTree(p).children.map(_.basicBlock.name))
        (root sdom domTree(p)) && (p != node._1)
      })
    }).values.toSet
    println(s"res:${res.map(_.name)}")
    res
  }

  def run(bb: BasicBlock): List[BasicBlock] = {
    val parents = findParents(domTree, domTree(bb))
    parents.toList.map(_.basicBlock)
//    val result = parents.map(node => findParents(domTree, node)).reduce(_ | _)
//    result.map(_.basicBlock).toList
  }
}
