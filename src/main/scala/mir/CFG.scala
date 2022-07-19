package rclang
package mir

def mkBB(name: String): BasicBlock = {
  val bb = BasicBlock(name, List(MultiSuccessorsInst()))
  bb
}

type BBsType = Map[String, BasicBlock]
def mkBBs(connections: (String, String)*): BBsType = {
  val set = connections.foldLeft(Set[BasicBlock]())((s, e) => s + mkBB(e._1) + mkBB(e._2))
  val map = set.map(s => s.name -> s).toMap
  connections.foreach((begin, end) => {
    map(begin).terminator.asInstanceOf[MultiSuccessorsInst].add(map(end))
  })
  map
}

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
