package rclang
package codegen

import mir.{Alloc, Argument}

class StackItem(var len: Int = 0, var offset: Int = 0) {}

case class LocalItem(private val _len: Int, alloc: Alloc) extends StackItem(_len) {
  override def toString: String = s"${alloc.id}"
}

case class ArgItem(private val _len: Int, arg: Argument) extends StackItem(_len) {
  override def toString: String = s"arg:${arg.name}"
}

case class TmpItem(private val _len: Int) extends StackItem(_len) {
  override def toString: String = "TmpItem"
}

case class MachineFrameInfo() {
  var items = List[StackItem]()

  def top = items.last

  def size = items.length

  def length = items.last.offset + items.last.len

  def addItem(item: StackItem): StackItem = {
    item.offset = if items.isEmpty then 0 else length
    items = items :+ item
    item
  }

  def checkValid = {
    items.sliding(3).foreach(window => {
      val valid = window.map(_.getClass).toSet.size != 3
      if(!valid) {
        throw new RuntimeException("invalid machine frame info")
      }
    })
  }

  def locals = sliceItems[LocalItem](items.indexWhere(_.isInstanceOf[LocalItem]), items.lastIndexWhere(_.isInstanceOf[LocalItem]))

  def tmps = sliceItems[TmpItem](items.indexWhere(_.isInstanceOf[TmpItem]), items.lastIndexWhere(_.isInstanceOf[TmpItem]))

  def args = sliceItems[ArgItem](items.indexWhere(_.isInstanceOf[ArgItem]), items.lastIndexWhere(_.isInstanceOf[ArgItem]))

  private def matchT[T](v: Object) = v match {
    case _: T => true
    case _ => false
  }


  private def sliceItems[T](range: (Int, Int)) = {
    if(range._1 == -1) {
      List()
    } else {
      items.slice(range._1, range._2).map(_.asInstanceOf[T])
    }
  }

  override def toString: String = {
    items.map(item => {
      val offsetLine = s"${item.offset.toString.padTo(4, ' ')}| ---------- |\n"
      val itemLine = s"    | ${item.toString.padTo(10, ' ')} |\n"
      val spaceLineCount: Int = item.len / 4
      //      val lenLine = (0 until spaceLineCount).toList.map("    |            |\n").mkString
      offsetLine + itemLine
    }).mkString+"    | ---------- |"
  }
}