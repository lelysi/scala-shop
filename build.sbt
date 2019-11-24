import Dependencies._

ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "scala-shop",
    libraryDependencies += scalaTest % Test
  )

libraryDependencies ++= Seq(akkaHttp, akkaStream, akkaStreamTestKit, akkaHttpTestKit, akkaSpray)