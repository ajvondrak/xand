name := "xand"
description := "One or the other are both true."
version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4"
)

initialCommands in console := "import xand._"
