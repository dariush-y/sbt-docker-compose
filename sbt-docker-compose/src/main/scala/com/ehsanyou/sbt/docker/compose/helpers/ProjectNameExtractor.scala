package com.ehsanyou.sbt.docker.compose.helpers

import com.ehsanyou.sbt.docker.compose.DataTypes.DockerComposeOption

object ProjectNameExtractor {

  def apply(
    defaultName: String,
    defaultOptions: Seq[DockerComposeOption],
    options: Seq[DockerComposeOption]
  ): DockerComposeOption = {

    val extractProjectName: DockerComposeOption = options
      .find(projectNameCollector)
      .orElse {
        defaultOptions
          .find(projectNameCollector)
      }
      .getOrElse(DockerComposeOption("-p", defaultName))

    val extractProjectNameSuffix: Option[DockerComposeOption] = options
      .find(projectNameSuffixCollector)
      .orElse {
        defaultOptions
          .find(projectNameSuffixCollector)
      }

    val projectName = extractProjectNameSuffix
      .fold(extractProjectName) { suffix =>
        extractProjectName.copy(
          value = extractProjectName.value
            .flatMap(name => suffix.value.map(s => name + s))
        )
      }

    projectName.copy(value = projectName.value.map(_.toLowerCase.replaceAll("[^a-zA-Z0-9]", "")))

  }

  private def projectNameCollector(option: DockerComposeOption) = option.key == "-p" || option.key == "--project-name"
  private def projectNameSuffixCollector(option: DockerComposeOption) = option.key == "--project-name-suffix"

}
