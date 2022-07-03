ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.1"

lazy val root = (project in file("."))
  .settings(
    name := "Rc-lang",
    idePackagePrefix := Some("rclang")
  )

scalacOptions += "-Ypartial-unification"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.0",
  "org.scalatest" %% "scalatest" % "3.2.9" % Test,
  "org.typelevel" %% "cats-core" % "2.7.0",
  "org.typelevel" %% "cats-effect" % "3.3.9"
)

libraryDependencies ++= Seq(
  "com.github.valskalla" %% "odin-core",
  "com.github.valskalla" %% "odin-json", //to enable JSON formatter if needed
  "com.github.valskalla" %% "odin-extras" //to enable additional features if needed (see docs)
).map(_ % "0.13.0")