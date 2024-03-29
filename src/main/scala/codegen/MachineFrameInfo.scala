package rclang
package codegen

import mir.{Alloc, Argument}

class StackItem(var len: Int = 0, var offset: Int = 0) {
  def toFrameIndex = {
    FrameIndex(offset, len)
  }
}

case class LocalItem(private val _len: Int, alloc: Alloc) extends StackItem(_len) {
  override def toString: String = s"local:${alloc.id}"
}

case class ArgItem(private val _len: Int, arg: Argument) extends StackItem(_len) {
  override def toString: String = s"arg:${arg.name}"
}

case class TmpItem(private val _len: Int) extends StackItem(_len) {
  override def toString: String = "TmpItem"
}

case class MachineFrameInfo() {
  var mf: MachineFunction = null

  var items = List[StackItem]()

  def isEmpty = items.isEmpty

  def top = items.last

  def size = items.length

  // 最后一个的位置，在其之上是数据，因此不需要添加len
  def length = items.last.offset

  def align(v: Int, base: Int) = (v / base + (if v % base == 0 then 0 else 1)) * base

  def alignLength = align(length, 16)

  // invalid
  //  -- 0 <- c  <== rsp
  //  -- 4 <- b
  //  -- 8 <- a  <== 低位
  // rsp - 8 = "abc"
  def addItem(item: StackItem): StackItem = {
    // todo: fix for 0 size
    item.offset = (if items.isEmpty then 0 else length) + (if item.len == 0 then 8 else item.len)
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

  private def sliceItems[T](range: (Int, Int)) = {
    if(range._1 == -1) {
      List()
    } else {
      items.slice(range._1, range._2 + 1).map(_.asInstanceOf[T])
    }
  }

  override def toString: String = {
    val line = "| ---------- |"
    s"${mf.name}\n0   $line rsp\n" +
    items.map(item => {
      val offsetLine = s"${item.offset.toString.padTo(4, ' ')}$line\n"
      val itemLine = s"    | ${item.toString.padTo(10, ' ')} |\n"
      val spaceLineCount: Int = item.len / 4

      val lenLine = (1 until spaceLineCount).map(n => s"${(item.offset + n * 4).toString.padTo(4, ' ')}|            |\n").mkString
      itemLine + offsetLine
    }).mkString
  }
}