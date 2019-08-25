lazy val scala213               = "2.13.0"
lazy val scala212               = "2.12.9"
lazy val scala211               = "2.11.12"

ThisBuild / organization := "com.desmondyeung.hashing"
ThisBuild / scalaVersion := scala213
ThisBuild / crossScalaVersions := List(scala213, scala212, scala211)

lazy val hashing = (project in file("."))
  .settings(
    name := "scala-hashing",
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 12 =>
          Seq(
            "-opt:l:method,inline",
            "-opt-inline-from:com.desmondyeung.hashing.**",
            "-opt-warnings",
            "-deprecation",
            "-Ywarn-dead-code")
        case _ =>
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

publishMavenStyle := true

publishTo in ThisBuild := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomIncludeRepository := { _ => false }

publishArtifact in Test := false

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
