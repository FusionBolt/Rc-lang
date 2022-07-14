package rclang
package mir

def canReach(a: BasicBlock, b: BasicBlock): Boolean = {
  if (a == b) return true
  a.successors.exists(canReach(_, b))
}


def dfsBasicBlocks(b: BasicBlock): List[BasicBlock] = {
  var result = List[BasicBlock]()
  var visited = Set[BasicBlock]()
  dfsImpl(b)
  def dfsImpl(b: BasicBlock): Unit = {
    if (visited(b)) return visited
    result = result :+ b
    visited = visited + b
    b.successors.foreach(dfsImpl)
  }
  result
}
