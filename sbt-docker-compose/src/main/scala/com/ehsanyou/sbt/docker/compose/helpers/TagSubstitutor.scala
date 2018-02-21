package com.ehsanyou.sbt.docker.compose.helpers

import java.io.File

import com.ehsanyou.sbt.docker.compose.DataTypes.DockerComposeOption
import com.ehsanyou.sbt.docker.compose.commands.dc.DockerComposeCmd
import com.ehsanyou.sbt.docker.compose.io.DockerComposeFileOps

object TagSubstitutor {

  def apply(
    dockerComposeCmd: DockerComposeCmd,
    dockerComposeFile: File,
    targetDir: File,
    tags: Seq[(String, String)],
    defaultTags: Seq[(String, String)]
  ): DockerComposeCmd = {

    val allTags = defaultTags ++ tags

    allTags.headOption.fold(dockerComposeCmd) { _ =>
      val file =
        DockerComposeFileOps(
          dockerComposeFile.getAbsolutePath,
          targetDir
        ).withImageTags(allTags).store

      dockerComposeCmd.withOption(DockerComposeOption("-f", file.getAbsolutePath))
    }
  }
}
