lazy val scala212               = "2.12.9"
lazy val scala211               = "2.11.12"
lazy val supportedScalaVersions = List(scala212, scala211)

ThisBuild / organization := "com.desmondyeung"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := scala212

lazy val hashing = (project in file("."))
  .settings(
    name := "Hashing",
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 12 ⇒
          Seq(
            "-opt:l:method,inline",
            "-opt-inline-from:com.desmondyeung.hashing.**",
            "-opt-warnings",
            "-deprecation",
            "-Ywarn-dead-code")
        case _ ⇒
          Seq("-optimize", "-deprecation", "-Ywarn-dead-code")
      }
    },
    javacOptions ++= Seq(
      "-encoding",
      "UTF-8",
      "-source",
      "1.8",
      "-target",
      "1.8"
    ),
    libraryDependencies ++= {
      Seq(
        "org.scalatest" %% "scalatest" % "3.0.8" % Test,
        "org.lz4"       % "lz4-java"   % "1.6.0" % Test
      )
    }
  )

lazy val bench = (project in file("bench"))
  .settings(
    libraryDependencies ++= {
      Seq(
        "com.google.guava" % "guava"                   % "28.0-jre",
        "org.lz4"          % "lz4-java"                % "1.6.0",
        "net.openhft"      % "zero-allocation-hashing" % "0.9"
      )
    }
  )
  .dependsOn(hashing % "compile->compile")
  .enablePlugins(JmhPlugin)

pomExtra in ThisBuild := (<url>https://github.com/desmondyeung/scala-hashing</url>
  <licenses>
    <license>
      <name>Apache 2</name>
      <url>https://github.com/desmondyeung/scala-hashing/blob/master/LICENSE</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:desmondyeung/scala-hashing.git</url>
    <connection>scm:git:git@github.com:desmondyeung/scala-hashing.git</connection>
  </scm>
  <developers>
    <developer>
      <id>desmondyeung</id>
      <name>Desmond Yeung</name>
      <url>https://github.com/desmondyeung</url>
    </developer>
  </developers>)
