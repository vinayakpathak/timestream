name := "timestream"
version := "0.0.1"
scalaVersion := "2.12.6"

//fork in run := true

libraryDependencies += "org.typelevel" %% "cats-core" % "1.0.1"
// available for Scala 2.11, 2.12
libraryDependencies += "co.fs2" %% "fs2-core" % "0.10.0-RC1" // For cats 1.0.1 and cats-effect 0.8

// optional I/O library
libraryDependencies += "co.fs2" %% "fs2-io" % "0.10.0-RC1"

libraryDependencies  ++= Seq(
  // Last stable release
  "org.scalanlp" %% "breeze" % "0.13.2",

  // Native libraries are not included by default. add this if you want them (as of 0.7)
  // Native libraries greatly improve performance, but increase jar sizes.
  // It also packages various blas implementations, which have licenses that may or may not
  // be compatible with the Apache License. No GPL code, as best I know.
  "org.scalanlp" %% "breeze-natives" % "0.13.2",

  // The visualization library is distributed separately as well.
  // It depends on LGPL code
  "org.scalanlp" %% "breeze-viz" % "0.13.2"
)


resolvers += "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"