import sbt.addCompilerPlugin

ThisBuild / scalaVersion := "2.13.1"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "example",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-mtl-core" % "0.7.0",
      "org.typelevel" %% "cats-effect" % "2.0.0",
      "org.typelevel" %% "cats-core" % "2.0.0"
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)
  )


