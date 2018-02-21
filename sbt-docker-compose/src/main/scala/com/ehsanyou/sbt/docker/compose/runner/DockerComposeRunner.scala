package com.ehsanyou.sbt.docker.compose.runner

import com.ehsanyou.sbt.docker.compose.DataTypes.Cwd
import com.ehsanyou.sbt.docker.compose.DataTypes.DockerComposeOption
import com.ehsanyou.sbt.docker.compose.commands.Command
import com.ehsanyou.sbt.docker.compose.commands.dc.DockerComposeCmd
import com.ehsanyou.sbt.docker.compose.commands.down.DockerComposeDownCmd
import com.ehsanyou.sbt.docker.compose.commands.up.DockerComposeUpCmd
import com.ehsanyou.sbt.docker.compose.helpers.ProjectNameExtractor
import sbt._

import scala.concurrent.Future
import scala.reflect.ClassTag

class DockerComposeRunner(
  dockerComposeCmd: DockerComposeCmd,
  command: Command,
  preConfiguredCommands: Seq[Command],
  defaultProjectName: String,
  dockerComposeFilePath: String,
  defaultTags: Seq[(String, String)],
  targetDir: File
)(
  implicit cwd: Cwd
) extends Runner {

  private val projectName: DockerComposeOption =
    ProjectNameExtractor(
      defaultProjectName,
      getCommand[DockerComposeCmd].underlying.options,
      dockerComposeCmd.underlying.options
    )

  private def getCommand[T <: Command: ClassTag] =
    preConfiguredCommands collect {
      case preCmd: T => preCmd
    } head

  override def run: Future[Def.Initialize[Task[Unit]]] =
    command match {
      case up: DockerComposeUpCmd =>
        new DockerComposeUpRunner(
          dockerComposeCmd,
          up,
          getCommand[DockerComposeUpCmd],
          dockerComposeFilePath,
          defaultTags,
          targetDir,
          projectName
        ).run

      case down: DockerComposeDownCmd =>
        new DockerComposeDownRunner(
          dockerComposeCmd,
          down,
          getCommand[DockerComposeDownCmd],
          projectName
        ).run
    }
}
