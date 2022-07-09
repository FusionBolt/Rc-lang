package rclang
package mir

case class DomTreeNode(var basicBlock: BasicBlock) {
  var parentTree: DomTree = null
  var domChildren: List[DomTreeNode] = List()
  var iDom: DomTreeNode = null
}

case class DomTree(var parent: Function, var entry: DomTreeNode) {
  entry.parentTree = this
  var nodes = Map[BasicBlock, DomTreeNode]()

  // todo:finish, should recalc
  def addNode(bb: BasicBlock) = {
    val node = DomTreeNode(bb)
    node.parentTree = this
    nodes += (bb -> node)
    node
  }

  def node(bb: BasicBlock): DomTreeNode = nodes(bb)

  private def isDomImpl(root: DomTreeNode, dest: DomTreeNode): Boolean = {
    if (root == dest) return true
    if (root.domChildren.isEmpty) return false
    root.domChildren.forall(isDomImpl(_, dest))
  }

  def isDom(i: DomTreeNode, a: DomTreeNode): Boolean = {
    assert(i.parentTree == a.parentTree && i.parentTree == this)
    isDomImpl(entry, i) && isDomImpl(i, a)
  }
}

case class DomTreeBuilder() {
  var visited = Set[BasicBlock]()
  def build(f: Function): DomTree = {
    val entry = DomTreeNode(f.entry)
    val dt = DomTree(f, entry)
    dfs(f.entry, dt, f)
    dt
  }

  private def dfs(root: BasicBlock, tree: DomTree, f: Function): DomTreeNode = {
    if(tree.nodes.contains(root)) {
      return tree.nodes(root)
    }

    var node = tree.addNode(root)
    val childs = root.terminator.successors.map(next => {
      dfs(next, tree, f)
    })
    node.domChildren = childs
    node
  }
}
