name := "sbt-docker-compose"

organization in ThisBuild := "com.github.ehsanyou"
scalaVersion in ThisBuild := "2.10.6"

lazy val baseDependencies = Seq(
  "com.spotify" % "docker-client" % "8.9.0",
  "io.circe" %% "circe-yaml" % "0.6.1",
  "com.typesafe.akka" %% "akka-actor" % "2.3.16",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

lazy val `sbt-docker-compose` = project
  .settings(
    sbtPlugin := true,
    libraryDependencies ++= baseDependencies
  )

lazy val root = (project in file("."))
  .settings(publishArtifact := false)
  .aggregate(
    `sbt-docker-compose`
  )