enablePlugins(JavaServerAppPackaging)

ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / version          := "0.1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .settings(
    name := "scala-shop",
  )

val akkaVersion = "2.5.25"
val akkaHttpVersion = "10.1.10"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.8",
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-http"   % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
  "com.typesafe.akka" %%  "akka-http-spray-json" % akkaHttpVersion,
  "com.github.t3hnar" %% "scala-bcrypt" % "4.1",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.pauldijou" %% "jwt-core" % "4.2.0"
)