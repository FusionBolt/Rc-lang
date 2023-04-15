package rclang
package compiler

import ast.RcModule
import tools.Render
import tools.DumpManager

class DependencyGraph(val modules: Seq[RcModule]) extends Render {
  case class Node(module: String)

  var graph = Map[String, List[String]]()

  def addEdge(src: String, dst: String): Unit = {
    graph = graph.updatedWith(src) {
      case Some(value) => Some(value :+ dst)
      case None => Some(List(dst))
    }
  }

  def checkCircle = {
    // 遍历每一个节点
    // 检查每一个节点开始，回到自己的路径
  }

  def rendGraph(fileName: String, directory: String) = {
    rend(fileName, directory, graph.toList) { (dot, relation) =>
      val (mod, refs) = relation
      dot.node(s"\"$mod\"")
      refs.foreach(ref => dot.edge(s"\"$mod\"", s"\"$ref\""))
    }
  }

  def moduleMap = modules.map(m => m.name -> m).toMap

  def moduleSet = modules.flatMap(_.refs).toSet
}

def dependencyResolve(modules: Seq[RcModule]) = {
  val graph = DependencyGraph(modules)
  modules.foreach(m => {
    m.refs.foreach(ref => {
      graph.addEdge(m.name, ref)
    })
  })
//  graph.rendGraph("dependency", DumpManager.getDumpRoot)
  graph
}