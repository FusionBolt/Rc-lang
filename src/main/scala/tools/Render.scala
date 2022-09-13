package rclang
package tools

trait Render {
  def rendInit(dot: Digraph) = {}
  def rendFinal(dot: Digraph) = {}

  protected def rendDotImpl(fileName: String, directory: String)(f: Digraph => Unit): Unit = {
    val dot = new Digraph()
    rendInit(dot)
    f(dot)
    rendFinal(dot)
    dot.render(fileName = fileName, directory = directory, format = "svg")
  }

  protected def rend[T](fileName: String, directory: String, v: T)(f: (Digraph, T) => Unit): Unit = {
    rendDotImpl(fileName, directory) { dot =>
      f(dot, v)
    }
  }

  protected def rend[T](fileName: String, directory: String, list: List[T])(f: (Digraph, T) => Unit): Unit = {
    rendDotImpl(fileName, directory) { dot =>
      list.foreach(v => f(dot, v))
    }
  }
}