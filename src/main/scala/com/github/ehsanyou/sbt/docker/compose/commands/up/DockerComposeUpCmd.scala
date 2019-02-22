package com.github.ehsanyou.sbt.docker.compose.commands.up

import com.github.ehsanyou.sbt.docker.compose.DataTypes.{DockerComposeOption, EnvironmentVariable}
import com.github.ehsanyou.sbt.docker.compose.commands.Command

case class DockerComposeUpCmd(underlying: DockerComposeUp = DockerComposeUp()) extends Command {

  override type CommandType = DockerComposeUpCmd

  override val name: String = "up"
  override val isEmpty: Boolean = underlying.options.isEmpty && underlying.services.isEmpty && underlying.tags.isEmpty

  override def build: String = Command.asString(name, underlying.options, underlying.services)()

  override def environment: Seq[(String, String)] = underlying.envVars.map(e => (e.key, e.value))

  override def hasEmptyOption: Boolean = underlying.options.isEmpty

  override def appendOption(option: DockerComposeOption): DockerComposeUpCmd =
    copy(underlying.copy(options = underlying.options :+ option))

  override def removeOption(optionKeys: String*): DockerComposeUpCmd = optionKeys.foldLeft(this) { (acc, nxt) =>
    acc.copy(acc.underlying.copy(options = acc.underlying.options.filterNot(_.key == nxt)))
  }
  override def appendServices(services: Seq[DockerComposeOption]): DockerComposeUpCmd =
    copy(underlying.copy(services = underlying.services ++ services))
  override def appendEnvVar(envVar: EnvironmentVariable): DockerComposeUpCmd =
    copy(underlying.copy(envVars = underlying.envVars :+ envVar))
}
