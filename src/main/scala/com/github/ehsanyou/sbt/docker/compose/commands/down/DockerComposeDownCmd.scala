package com.github.ehsanyou.sbt.docker.compose.commands.down

import com.github.ehsanyou.sbt.docker.compose.DataTypes.DockerComposeOption
import com.github.ehsanyou.sbt.docker.compose.commands.Command

case class DockerComposeDownCmd(underlying: DockerComposeDown = DockerComposeDown()) extends Command {

  override type CommandType = DockerComposeDownCmd

  override val name: String = "down"
  override val isEmpty: Boolean = underlying.options.isEmpty

  override def build: Seq[String] = Command.asStringSeq(name, underlying.options, underlying.services)()

  override def hasEmptyOption: Boolean = underlying.options.isEmpty && underlying.services.isEmpty

  override def appendOption(option: DockerComposeOption): DockerComposeDownCmd =
    DockerComposeDownCmd(underlying.copy(options = underlying.options :+ option))

  override def removeOption(optionKeys: String*): DockerComposeDownCmd = optionKeys.foldLeft(this) { (acc, nxt) =>
    acc.copy(acc.underlying.copy(options = acc.underlying.options.filterNot(_.key == nxt)))
  }

  override def appendServices(services: Seq[DockerComposeOption]): DockerComposeDownCmd =
    copy(underlying.copy(services = underlying.services ++ services))
}
