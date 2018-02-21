package com.github.ehsanyou.sbt.docker.compose.runner

import com.github.ehsanyou.sbt.docker.compose.DataTypes.Cwd
import com.github.ehsanyou.sbt.docker.compose.DataTypes.DockerComposeOption
import com.github.ehsanyou.sbt.docker.compose.commands.dc.DockerComposeCmd
import com.github.ehsanyou.sbt.docker.compose.commands.down.DockerComposeDownCmd
import com.github.ehsanyou.sbt.docker.compose.helpers._
import sbt._

import scala.concurrent.Future

class DockerComposeDownRunner(
  dockerComposeCmd: DockerComposeCmd,
  dockerComposeDownCmd: DockerComposeDownCmd,
  preConfiguredDockerComposeDownCmd: DockerComposeDownCmd,
  projectDockerComposeOption: DockerComposeOption
)(
  implicit cwd: Cwd
) extends Runner {

  override def run: Future[Def.Initialize[Task[Unit]]] =
    process(command)(sbtFutureTask.empty)

  private val command: String =
    if (dockerComposeDownCmd.hasEmptyOption) {
      dockerComposeCmd.withProjectName(projectDockerComposeOption) combine dockerComposeDownCmd.copy(
        dockerComposeDownCmd.underlying.copy(options = preConfiguredDockerComposeDownCmd.underlying.options)
      )
    } else dockerComposeCmd.withProjectName(projectDockerComposeOption) combine dockerComposeDownCmd
}
