package com.github.ehsanyou.sbt.docker.compose.commands.dc

import com.github.ehsanyou.sbt.docker.compose.DataTypes.DockerComposeOption
import com.github.ehsanyou.sbt.docker.compose.commands.Command

case class DockerComposeCmd(underlying: DockerCompose = DockerCompose()) extends Command {

  override type CommandType = DockerComposeCmd

  override def name: String = "docker-compose"
  override val isEmpty: Boolean = underlying.options.isEmpty
  override def build: Seq[String] =
    Command.asStringSeq(name, underlying.options)(DockerCompose.nonNativeOptions.flatMap(_.keys): _*)
  override def hasEmptyOption: Boolean = underlying.options.isEmpty

  private[compose] def withComposeFiles(
    filePaths: String*
  ): DockerComposeCmd =
    if (underlying.options.exists(x => !(x.key == "-f" || x.key == "--file")))
      filePaths.foldLeft(this)((_, nxt) => appendOption(DockerComposeOption("-f", nxt)))
    else this

  private[compose] def withProjectName(projectName: DockerComposeOption): DockerComposeCmd =
    removeOption("-p", "--project-name").withOption(projectName)

  private[compose] def withProjectName(projectName: String): DockerComposeCmd =
    withProjectName(DockerComposeOption("-p", projectName))

  private[compose] def fallback(cmd: DockerComposeCmd): DockerComposeCmd =
    if (isEmpty) cmd
    else this

  override def appendOption(option: DockerComposeOption): DockerComposeCmd =
    copy(underlying.copy(options = underlying.options :+ option))

  override def removeOption(optionKeys: String*): DockerComposeCmd =
    optionKeys.foldLeft(this) { (acc, nxt) =>
      acc.copy(acc.underlying.copy(options = acc.underlying.options.filterNot(_.key == nxt)))
    }

  override def appendServices(services: Seq[DockerComposeOption]): DockerComposeCmd = this
}
