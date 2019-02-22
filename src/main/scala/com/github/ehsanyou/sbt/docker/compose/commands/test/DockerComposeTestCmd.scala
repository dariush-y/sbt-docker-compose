package com.github.ehsanyou.sbt.docker.compose.commands.test

import com.github.ehsanyou.sbt.docker.compose.DataTypes.{DockerComposeOption, EnvironmentVariable}
import com.github.ehsanyou.sbt.docker.compose.commands.Command
import com.github.ehsanyou.sbt.docker.compose.commands.test.DockerComposeTest._

case class DockerComposeTestCmd(underlying: DockerComposeTest) extends Command {

  override type CommandType = DockerComposeTestCmd

  override val name: String = "up"
  override val isEmpty: Boolean = underlying.options.isEmpty && underlying.services.isEmpty && underlying.tags.isEmpty

  override def build: String = Command.asString(name, underlying.options, underlying.services)()

  override def environment: Seq[(String, String)] = underlying.envVars.map(e => (e.key, e.value))

  override def hasEmptyOption: Boolean = underlying.options.isEmpty

  def withTestOnly(arg: String): DockerComposeTestCmd = withTestType(TestOnly(arg))

  def withTest: DockerComposeTestCmd = withTestType(Test)

  def withTestType(testType: TestType): DockerComposeTestCmd =
    new DockerComposeTestCmd(underlying.copy(testType = testType))

  override def appendOption(option: DockerComposeOption): DockerComposeTestCmd =
    copy(underlying.copy(options = underlying.options :+ option))

  override def removeOption(optionKeys: String*): DockerComposeTestCmd = optionKeys.foldLeft(this) { (acc, nxt) =>
    acc.copy(acc.underlying.copy(options = acc.underlying.options.filterNot(_.key == nxt)))
  }

  override def appendServices(services: Seq[DockerComposeOption]): DockerComposeTestCmd =
    copy(underlying.copy(services = underlying.services ++ services))

  override def appendEnvVar(envVar: EnvironmentVariable): DockerComposeTestCmd =
    copy(underlying.copy(envVars = underlying.envVars :+ envVar))
}

object DockerComposeTestCmd {
  def apply(testType: TestType): DockerComposeTestCmd = new DockerComposeTestCmd(DockerComposeTest(testType))
}
