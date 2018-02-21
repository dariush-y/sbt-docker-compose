package com.github.ehsanyou.sbt.docker.compose.commands

import com.github.ehsanyou.sbt.docker.compose.DataTypes.DockerComposeOption
import com.github.ehsanyou.sbt.docker.compose.DataTypes.DockerComposeOptionKey

trait Command {

  type CommandType

  def name: String

  def build: String

  def isEmpty: Boolean

  def nonEmpty: Boolean = !isEmpty

  def hasEmptyOption: Boolean

  def combine(cmd: Command): String = this.build + " " + cmd.build

  def withOption(option: DockerComposeOption): CommandType = appendOption(option)

  def withOption(option: String): CommandType = appendOption(DockerComposeOption(option))

  def withOption(option: (String, String)): CommandType = appendOption(DockerComposeOption(option._1, option._2))

  def withServices(services: Seq[String]): CommandType = appendServices(services.map(DockerComposeOption.apply))

  def appendOption(option: DockerComposeOption): CommandType

  def removeOption(optionKeys: String*): CommandType

  def appendServices(services: Seq[DockerComposeOption]): CommandType

}

object Command {

  def asString(name: String, args: Seq[DockerComposeOption]*)(optionsToOmit: String*): String = {
    val flatArgs = args.flatten.filterNot(opt => optionsToOmit.exists(_ == opt.key))
    if (flatArgs.isEmpty) {
      name
    } else name + whiteSpace + flatArgs.map(_.toString).mkString(whiteSpace)
  }

  private val whiteSpace = " "

}

trait CommandCompanion {
  val options: Seq[DockerComposeOptionKey]
}
