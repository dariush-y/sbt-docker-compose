package com.github.ehsanyou.sbt.docker.compose.runner

import akka.actor.ActorSystem
import com.github.ehsanyou.sbt.docker.compose.DataTypes.Cwd
import com.github.ehsanyou.sbt.docker.compose.DataTypes.DockerComposeOption
import com.github.ehsanyou.sbt.docker.compose.HealthCheckActor
import com.github.ehsanyou.sbt.docker.compose.HealthCheckActor.Protocol
import com.github.ehsanyou.sbt.docker.compose.commands.dc.DockerComposeCmd
import com.github.ehsanyou.sbt.docker.compose.commands.test.DockerComposeTest.ItTest
import com.github.ehsanyou.sbt.docker.compose.commands.test.DockerComposeTest.ItTestOnly
import com.github.ehsanyou.sbt.docker.compose.commands.test.DockerComposeTest.ItTestQuick
import com.github.ehsanyou.sbt.docker.compose.commands.test.DockerComposeTest.TestOnly
import com.github.ehsanyou.sbt.docker.compose.commands.test.DockerComposeTest.TestQuick
import com.github.ehsanyou.sbt.docker.compose.commands.test.DockerComposeTest.{Test => TestDT}
import com.github.ehsanyou.sbt.docker.compose.commands.test.DockerComposeTestCmd
import com.github.ehsanyou.sbt.docker.compose.docker.client.DockerClient
import com.github.ehsanyou.sbt.docker.compose.docker.client.IDockerClient
import com.github.ehsanyou.sbt.docker.compose.helpers.ProjectNameExtractor
import com.github.ehsanyou.sbt.docker.compose.helpers._
import sbt.Keys._
import sbt.Def
import sbt._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class DockerComposeTestRunner(
  dockerComposeCommand: DockerComposeCmd,
  defaultDockerComposeCommand: DockerComposeCmd,
  dockerComposeTestCommand: DockerComposeTestCmd,
  preConfiguredDockerComposeTestCmd: DockerComposeTestCmd,
  defaultProjectName: String,
  deadline: FiniteDuration,
  dockerComposeFilePath: String,
  targetDir: File,
  defaultTags: Seq[(String, String)],
  quickModeEnabled: Boolean,
  consoleLoggerEnabled: Boolean
)(
  implicit actorSystem: ActorSystem,
  executionContext: ExecutionContext,
  cwd: Cwd
) extends Runner {

  override def run: Future[Def.Initialize[Task[Unit]]] =
    process(command)(preConfiguredDockerComposeTestCmd.environment) {

      if (consoleLoggerEnabled) processNonBlocking(s"docker-compose $projectName logs -f --tail=all")

      SystemPropertyProvider(dockerClient.containers)

      dockerComposeTestCommand.underlying.testType match {
        case TestDT | TestOnly(_) | TestQuick => runTest(Test)
        case ItTest | ItTestOnly(_) | ItTestQuick => runTest(IntegrationTest)
      }
    } map { task =>
      if (quickModeEnabled)
        task
      else
        task.andFinally {
          dockerComposeDown
        }
    }

  private def runTest(config: Configuration): Future[Def.Initialize[Task[Unit]]] = {
    greenPrinter("Health check...")
    HealthCheckActor
      .healthCheck(dockerClient.inspectContainers, deadline)
      .map {
        case Right(_) =>
          testTask(config)
        case Left(unHealthy) =>
          dockerComposeDown
          throwUnhealthyContainers(unHealthy)
      }
  }

  private def throwUnhealthyContainers(unHealthy: Protocol.UnHealthy): Nothing = {
    val containers = unHealthy.unhealthyContainers
      .map(c => s"${c.name()}:${c.id()}")
      .mkString(" ,")
    throw new IllegalStateException(s"Some containers are unhealthy! [$containers].")
  }

  private def testTask(config: Configuration): Def.Initialize[Task[Unit]] = Def.taskDyn {

    config match {
      case Test =>
        greenPrinter("running tests...")
        dockerComposeTestCommand.underlying.testType match {
          case TestDT =>
            (test in Test).toTask
          case TestQuick =>
            (testQuick in Test).toTask("")
          case TestOnly(arg) =>
            (testOnly in Test).toTask(s" $arg")
          case _ =>
            sys.error(
              s"This looks like a bug in the plugin. ${dockerComposeTestCommand.underlying.testType} is used in Test scope"
            )
        }

      case IntegrationTest =>
        greenPrinter("running integration tests...")
        dockerComposeTestCommand.underlying.testType match {
          case ItTest =>
            (test in IntegrationTest).toTask
          case ItTestQuick =>
            (testQuick in IntegrationTest).toTask("")
          case ItTestOnly(arg) =>
            (testOnly in IntegrationTest).toTask(s" $arg")
          case _ =>
            sys.error(
              s"This looks like a bug in the plugin. ${dockerComposeTestCommand.underlying.testType} is used in IntegrationTest scope"
            )
        }
    }
  }

  private val projectName: DockerComposeOption =
    ProjectNameExtractor(
      defaultProjectName,
      defaultDockerComposeCommand.underlying.options,
      dockerComposeCommand.underlying.options
    )

  private implicit val dockerClient: IDockerClient = new DockerClient(projectName.value.get)

  private def dockerComposeDown =
    process(
      s"docker-compose $projectName -f $dockerComposeFilePath down --remove-orphans"
    )(Seq.empty)(())

  private val command: String = {

    lazy val cmd: DockerComposeCmd = TagSubstitutor(
      dockerComposeCmd = dockerComposeCommand,
      dockerComposeFile = new File(dockerComposeFilePath),
      targetDir = targetDir,
      tags = dockerComposeTestCommand.underlying.tags,
      defaultTags = defaultTags
    ).withProjectName(projectName)

    if (dockerComposeTestCommand.hasEmptyOption)
      cmd combine {
        dockerComposeTestCommand
          .copy(
            dockerComposeTestCommand.underlying.copy(options = preConfiguredDockerComposeTestCmd.underlying.options)
          )
          .removeOption("-d")
          .withOption(DockerComposeOption("-d"))
      } else
      cmd combine {
        dockerComposeTestCommand
          .removeOption("-d")
          .withOption(DockerComposeOption("-d"))
      }
  }
}
