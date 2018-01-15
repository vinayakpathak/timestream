name := "timestream"
version := "0.0.1"
scalaVersion := "2.11.8"

//fork in run := true

libraryDependencies += "org.typelevel" %% "cats-core" % "1.0.1"
libraryDependencies += "co.fs2" %% "fs2-core" % "0.9.7"
libraryDependencies += "co.fs2" %% "fs2-io" % "0.9.7"
libraryDependencies += "org.typelevel" %% "cats-effect" % "0.5"