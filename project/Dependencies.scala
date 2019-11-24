import sbt._

object Dependencies {
  val akkaVersion = "2.5.23"
  val akkaHttpVersion = "10.1.10"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8"
  lazy val akkaHttp = "com.typesafe.akka" %% "akka-http"   % akkaHttpVersion
  lazy val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVersion
  lazy val akkaStreamTestKit = "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion
  lazy val akkaHttpTestKit = "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion
  lazy val akkaSpray = "com.typesafe.akka" %%  "akka-http-spray-json" % akkaHttpVersion
}
