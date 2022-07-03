package rclang
package tools

import java.nio.file.{Files, Paths}

object DumpManager {
  var dumpRoot = "RcDump"
  def mkDumpRootDir = {
    Files.createDirectories(Paths.get(dumpRoot))
  }
}
