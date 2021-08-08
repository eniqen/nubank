import com.typesafe.sbt.packager.docker.{Cmd, ExecCmd}

ThisBuild / organization := "com.nubank"
ThisBuild / name         := "nubank-assignment"
ThisBuild / version := "0.1"
scalaVersion := "2.13.1"

scalacOptions += "-Ymacro-annotations"

libraryDependencies ++= Dependencies.live

Dependencies.CompilerPlugins.live.map(addCompilerPlugin)

resolvers += Resolver.sonatypeRepo("snapshots")

enablePlugins(JavaAppPackaging, JDKPackagerPlugin, DockerPlugin)

dockerExposedPorts ++= Seq(8080)
dockerBaseImage := "openjdk:8-jre-alpine"
dockerCommands ++= Seq(
  Cmd("USER", "root"),
  ExecCmd("RUN", "apk", "add", "--no-cache", "bash")
)

val mainPath                = "com.nubank.assignment.AuthorizerApp"
mainClass       in Compile  := Some(mainPath)
mainClass       in assembly := Some(mainPath)
assemblyJarName in assembly := s"${name.value}-${version.value}.jar"
conflictManager             := ConflictManager.latestRevision