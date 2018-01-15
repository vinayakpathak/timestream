name := "timestream"
version := "0.0.1"
scalaVersion := "2.11.8"

//fork in run := true

libraryDependencies += "org.typelevel" %% "cats-core" % "1.0.1"
// available for Scala 2.11, 2.12
libraryDependencies += "co.fs2" %% "fs2-core" % "0.10.0-RC1" // For cats 1.0.1 and cats-effect 0.8

// optional I/O library
libraryDependencies += "co.fs2" %% "fs2-io" % "0.10.0-RC1"