package com.ehsanyou.sbt.docker.compose.runner

import com.ehsanyou.sbt.docker.compose.DataTypes.Cwd
import com.ehsanyou.sbt.docker.compose.DataTypes.DockerComposeOption
import com.ehsanyou.sbt.docker.compose.commands.dc.DockerComposeCmd
import com.ehsanyou.sbt.docker.compose.commands.up.DockerComposeUpCmd
import com.ehsanyou.sbt.docker.compose.helpers._
import sbt._

import scala.concurrent.Future

class DockerComposeUpRunner(
  dockerComposeCmd: DockerComposeCmd,
  dockerComposeUpCmd: DockerComposeUpCmd,
  preConfiguredDockerComposeUpCmd: DockerComposeUpCmd,
  dockerComposeFilePath: String,
  defaultTags: Seq[(String, String)],
  targetDir: File,
  projectName: DockerComposeOption
)(
  implicit cwd: Cwd
) extends Runner {

  override def run: Future[Def.Initialize[Task[Unit]]] =
    process(command)(sbtFutureTask.empty)

  lazy val dockerComposeCmdWithTagSubstitution = TagSubstitutor(
    dockerComposeCmd.withProjectName(projectName),
    new File(dockerComposeFilePath),
    targetDir,
    dockerComposeUpCmd.underlying.tags,
    defaultTags
  )

  private lazy val command: String =
    if (dockerComposeUpCmd.hasEmptyOption) {
      dockerComposeCmdWithTagSubstitution.withProjectName(projectName) combine dockerComposeUpCmd.copy(
        dockerComposeUpCmd.underlying.copy(options = preConfiguredDockerComposeUpCmd.underlying.options)
      )
    } else dockerComposeCmdWithTagSubstitution.withProjectName(projectName) combine dockerComposeUpCmd
}
