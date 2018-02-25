import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

name := "sbt-docker-compose"
organization in ThisBuild := "com.github.ehsanyou"
sbtPlugin := true
sbtVersion in Global := "0.13.7"
crossSbtVersions := List("0.13.17", "1.1.0")
scalaCompilerBridgeSource := {
  val sv = appConfiguration.value.provider.id.version
  ("org.scala-sbt" % "compiler-interface" % sv % "component").sources
}
scalaVersion := (CrossVersion partialVersion (sbtVersion in pluginCrossBuild).value match {
  case Some((0, 13)) => "2.10.6"
  case Some((1, _)) => "2.12.3"
  case _ => sys error s"Unhandled sbt version ${(sbtVersion in pluginCrossBuild).value}"
})

def akkaActor(scalaVersion: String) = CrossVersion.partialVersion(scalaVersion) match {
  case Some((2, n)) if n < 12 =>
    "com.typesafe.akka" %% "akka-actor" % "2.3.16"
  case _ =>
    "com.typesafe.akka" %% "akka-actor" % "2.4.20"
}

libraryDependencies ++= Seq(
  "com.spotify" % "docker-client" % "8.9.0",
  "io.circe" %% "circe-yaml" % "0.6.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  akkaActor(scalaVersion.value)
)

publishMavenStyle := true
publishArtifact in Test := false
useGpg := true

publishTo := {
  val nexus = "https://oss.sonatype.org"
  if (isSnapshot.value)
    Some("snapshots" at s"$nexus/content/repositories/snapshots")
  else
    Some("releases" at s"$nexus/service/local/staging/deploy/maven2")
}

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
)

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

