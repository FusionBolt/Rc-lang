package rclang
package mir
import scala.collection.mutable.LinkedHashMap
import scala.collection.mutable.LinkedHashSet

def mkBB(name: String): BasicBlock = {
  val bb = BasicBlock(name, List(MultiSuccessorsInst()))
  bb
}

type BBsType = LinkedHashMap[String, BasicBlock]
def mkBBs(connections: (String, String)*): BBsType = {
  // sorted by name
  val set = connections.foldLeft(Set[BasicBlock]())((s, e) => s + mkBB(e._1) + mkBB(e._2)).filter(b => b.name != "entry" && b.name != "exit")
  var bbMap = LinkedHashMap("entry" -> mkBB("entry"))
  val bbInFn = LinkedHashMap.from(set.toList.sortBy(_.name).map(s => s.name -> s))
  bbMap = bbMap ++ bbInFn
  bbMap("exit") = mkBB("exit")
  connections.foreach((begin, end) => {
    bbMap(begin).terminator.asInstanceOf[MultiSuccessorsInst].add(bbMap(end))
  })
  bbMap
}

def canReach(a: BasicBlock, b: BasicBlock): Boolean = {
  if (a == b) return true
  a.successors.exists(canReach(_, b))
}

def predecessors(bb: BasicBlock, bbs: List[BasicBlock]): LinkedHashSet[BasicBlock] = {
  LinkedHashSet.from(bbs.filter(_.terminator.successors.contains(bb)))
}

def predecessorsMap(bbs: List[BasicBlock]): Map[BasicBlock, LinkedHashSet[BasicBlock]] = {
  bbs.map(bb => bb -> predecessors(bb, bbs)).toMap
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
