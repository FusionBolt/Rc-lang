package rclang
package ast

import tools.Render

class ClassesRender extends Render {
  def rendClasses(fileName: String, directory: String, methods: List[Class]): Unit = {
    rend(fileName, directory, methods) { (dot, klass) =>
      dot.node(klass.name.str)
      klass.parent match
        case Some(parent) => dot.edge(parent.str, klass.name.str)
        case _ =>
    }
  }
}
