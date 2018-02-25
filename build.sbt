import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

name := "sbt-docker-compose"
organization in ThisBuild := "com.github.ehsanyou"
version := "1.0.0"

lazy val publishSetting = Seq(
  publishTo := {
    val nexus = "https://oss.sonatype.org"
    if (isSnapshot.value)
      Some("snapshots" at s"$nexus/content/repositories/snapshots")
    else
      Some("releases" at s"$nexus/service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false,
  publishMavenStyle := true,
  useGpg := true,
  releaseProcess := Seq(
    checkSnapshotDependencies,
    inquireVersions,
    releaseStepCommandAndRemaining("^test"),
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("^publishSigned"),
    setNextVersion,
    commitNextVersion,
    ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
    pushChanges
  ),
  pomExtra := {
    <url>http://github.com/ehsanyou/sbt-docker-compose</url>
      <licenses>
        <license>
          <name>MIT license</name>
          <url>http://www.opensource.org/licenses/mit-license.php</url>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:ehsanyou/sbt-docker-compose.git</url>
        <connection>scm:git:git@github.com:ehsanyou/sbt-docker-compose.git</connection>
      </scm>
      <developers>
        <developer>
          <id>ehsan.yousefi@live.com</id>
          <name>Ehsan Yousefi</name>
          <url>http://github.com/ehsanyou</url>
        </developer>
      </developers>
  }
)

def akkaActor(scalaVersion: String) = scalaVersion match {
  case "2.10.6" =>
    "com.typesafe.akka" %% "akka-actor" % "2.3.16"
  case _ =>
    "com.typesafe.akka" %% "akka-actor" % "2.5.10"
}

def circeYaml(scalaVersion: String) = scalaVersion match {
  case "2.10.6" =>
    "io.circe" % "circe-yaml_2.10" % "0.6.1" exclude("org.spire-math", "jawn-parser_2.10")
  case _ =>
    "io.circe" % "circe-yaml_2.12" % "0.6.1" exclude("org.spire-math", "jawn-parser_2.12")
}

lazy val baseDependencies = Seq(
  "com.spotify" % "docker-client" % "8.9.0",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

lazy val `sbt-docker-compose` = (project in file("."))
  .settings(
    sbtPlugin := true,
    scalaVersion := "2.10.6",
    crossSbtVersions := Vector("0.13.16", "1.0.0"),
    scalaCompilerBridgeSource := {
      val sv = appConfiguration.value.provider.id.version
      ("org.scala-sbt" % "compiler-interface" % sv % "component").sources
    },
    libraryDependencies += circeYaml(scalaVersion.value),
    libraryDependencies ++= baseDependencies :+ akkaActor(scalaVersion.value)
  )
  .settings(publishSetting: _*)