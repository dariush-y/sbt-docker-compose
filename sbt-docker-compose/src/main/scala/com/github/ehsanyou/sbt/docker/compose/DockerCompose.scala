package com.github.ehsanyou.sbt.docker.compose

import java.util.UUID

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import com.github.ehsanyou.sbt.docker.compose.DataTypes.Cwd
import com.github.ehsanyou.sbt.docker.compose.DataTypes.DockerComposeOption
import com.github.ehsanyou.sbt.docker.compose.commands.dc.DockerComposeCmd
import com.github.ehsanyou.sbt.docker.compose.commands.down.DockerComposeDownCmd
import com.github.ehsanyou.sbt.docker.compose.commands.test.DockerComposeTestCmd
import com.github.ehsanyou.sbt.docker.compose.commands.up.DockerComposeUpCmd
import com.github.ehsanyou.sbt.docker.compose.helpers._
import com.github.ehsanyou.sbt.docker.compose.runner.DockerComposeRunner
import com.github.ehsanyou.sbt.docker.compose.runner.DockerComposeTestRunner
import sbt.Keys._
import sbt.{Def, _}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext

object DockerCompose extends AutoPlugin {

  import autoImport._

  override def requires = empty

  override lazy val projectSettings = Seq(
    dockerCompose := dockerComposeImpl.evaluated,
    dockerComposeFilePath := dockerComposeFilePathImpl.value,
    dockerComposeProjectName := dockerComposeProjectNameImpl.value,
    dockerComposeCommandOptions := dockerComposeCommandOptionsImpl.value,
    dockerComposeUpCommandOptions := dockerComposeUpCommandOptionsImpl.value,
    dockerComposeDownCommandOptions := dockerComposeDownCommandOptionsImpl.value,
    dockerComposeTags := dockerComposeTagsImpl.value,
    dockerComposeIgnore := false,
    dockerComposeTest := dockerComposeTestImpl(false).evaluated,
    dockerComposeTestQuick := dockerComposeTestImpl(true).evaluated,
    dockerComposeTestCommandOptions := dockerComposeTestCommandOptionsImpl.value,
    dockerComposeHealthCheckDeadline := dockerComposeHealthCheckDeadlineImpl.value,
    dockerComposeTestDummy := dockerComposeTestDummyImpl.value,
    dockerComposeTestLogging := false
  )

  object autoImport {

    val dockerCompose = Keys.dockerCompose
    val dockerComposeFilePath = Keys.dockerComposeFilePath
    val dockerComposeProjectName = Keys.dockerComposeProjectName
    val dockerComposeCommandOptions = Keys.dockerComposeCommandOptions
    val dockerComposeUpCommandOptions = Keys.dockerComposeUpCommandOptions
    val dockerComposeDownCommandOptions = Keys.dockerComposeDownCommandOptions
    val dockerComposeIgnore = Keys.dockerComposeIgnore
    val dockerComposeTags = Keys.dockerComposeTags

    val dockerComposeTest = Keys.dockerComposeTest
    val dockerComposeTestQuick = Keys.dockerComposeTestQuick
    val dockerComposeTestCommandOptions = Keys.dockerComposeTestCommandOptions
    val dockerComposeHealthCheckDeadline = Keys.dockerComposeHealthCheckDeadline
    val dockerComposeTestDummy = Keys.dockerComposeTestDummy
    val dockerComposeTestLogging = Keys.dockerComposeTestLogging
    val DockerComposeTestTag = Tags.Tag("DockerComposeTest")

  }

  implicit lazy val actorSystem: ActorSystem = {

    val cl: ClassLoader = getClass.getClassLoader
    val ac: ActorSystem = ActorSystem(s"sbt-docker-compose-${UUID.randomUUID()}", ConfigFactory.load(cl), cl)

    scala.sys.addShutdownHook {
      ac.terminate()
    }

    ac
  }

  implicit lazy val executionContext: ExecutionContext = actorSystem.dispatcher

  lazy val defaultTimeout: FiniteDuration = 30 seconds

  lazy val dockerComposeTagsImpl = Def.setting {
    Seq.empty[(String, String)]
  }

  lazy val dockerComposeDownCommandOptionsImpl: Def.Initialize[DockerComposeDownCmd] = Def.setting {
    DockerComposeDownCmd()
  }

  lazy val dockerComposeCommandOptionsImpl: Def.Initialize[DockerComposeCmd] = Def.setting {
    DockerComposeCmd()
      .withOption(DockerComposeOption("-p", dockerComposeProjectName.value))
  }

  lazy val dockerComposeUpCommandOptionsImpl: Def.Initialize[DockerComposeUpCmd] = Def.setting {
    DockerComposeUpCmd()
  }

  lazy val dockerComposeProjectNameImpl: Def.Initialize[String] = Def.setting {
    name.value
  }

  lazy val dockerComposeFilePathImpl: Def.Initialize[String] = Def.setting {
    val basePath: String = baseDirectory.value.absolutePath
    basePath + "/docker-compose.yml"
  }

  lazy val dockerComposeTestDummyImpl: Def.Initialize[Task[Unit]] = Def.task(())

  lazy val dockerComposeHealthCheckDeadlineImpl: Def.Initialize[FiniteDuration] = Def setting 5.minutes

  lazy val dockerComposeTestCommandOptionsImpl: Def.Initialize[DockerComposeTestCmd] = Def.setting {
    DockerComposeTestCmd(commands.test.DockerComposeTest.Test)
  }

  lazy val dockerComposeImpl = Def.inputTaskDyn {

    val (dockerComposeCmd, command) = parsers.dockerComposeParser.parsed

    if (!dockerComposeIgnore.value) {

      implicit val cwd: Cwd = Cwd(baseDirectory.value)
      val preConfiguredCommands = Seq(
        dockerComposeCommandOptions.value,
        dockerComposeDownCommandOptions.value,
        dockerComposeUpCommandOptions.value
      )

      Await.result(
        new DockerComposeRunner(
          dockerComposeCmd
            .fallback(dockerComposeCommandOptions.value)
            .withComposeFiles(dockerComposeFilePath.value),
          command,
          preConfiguredCommands,
          dockerComposeProjectName.value,
          dockerComposeFilePath.value,
          dockerComposeTags.value,
          target.value
        ).run,
        defaultTimeout
      )
    } else sbtTask.empty
  }

  def dockerComposeTestImpl(quickMode: Boolean): Def.Initialize[InputTask[Unit]] =
    Def.inputTaskDyn {

      val (dockerComposeCommand, dockerComposeTestCommand) = parsers.dockerComposeTestParser.parsed

      if (!dockerComposeIgnore.value) {

        implicit val cwd: Cwd = Cwd(baseDirectory.value)

        Await.result(
          new DockerComposeTestRunner(
            dockerComposeCommand
              .fallback(dockerComposeCommandOptions.value)
              .withComposeFiles(dockerComposeFilePath.value),
            dockerComposeCommandOptions.value,
            dockerComposeTestCommand,
            dockerComposeTestCommandOptions.value,
            dockerComposeProjectName.value,
            dockerComposeHealthCheckDeadline.value,
            dockerComposeFilePath.value,
            target.value,
            dockerComposeTags.value,
            quickMode,
            dockerComposeTestLogging.value
          ).run,
          dockerComposeHealthCheckDeadline.value.plus(30 seconds)
        )

      } else sbtTask.empty
    } tag DockerComposeTestTag
}
