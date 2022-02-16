ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.1"

lazy val root = (project in file("."))
  .settings(
    name := "Rc-lang",
    idePackagePrefix := Some("rclang")
  )

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.0",
  "org.scalatest" %% "scalatest" % "3.2.9" % Test
)