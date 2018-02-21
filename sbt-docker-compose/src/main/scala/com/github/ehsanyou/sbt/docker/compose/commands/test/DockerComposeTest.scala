package com.github.ehsanyou.sbt.docker.compose.commands.test

import com.github.ehsanyou.sbt.docker.compose.DataTypes._
import com.github.ehsanyou.sbt.docker.compose.commands.CommandCompanion
import com.github.ehsanyou.sbt.docker.compose.commands.test.DockerComposeTest.TestType
import com.github.ehsanyou.sbt.docker.compose.commands.up.DockerComposeUp

case class DockerComposeTest(
  options: Seq[DockerComposeOption],
  services: Seq[DockerComposeOption],
  tags: Seq[(String, String)],
  testType: TestType
)

object DockerComposeTest extends CommandCompanion {

  def apply(testType: TestType): DockerComposeTest = DockerComposeTest(Seq.empty, Seq.empty, Seq.empty, testType)

  override val options: Seq[DockerComposeOptionKey] = DockerComposeUp.options

  sealed trait TestType

  case class TestOnly(arg: String) extends TestType

  case object Test extends TestType

  case object TestQuick extends TestType

  case class ItTestOnly(arg: String) extends TestType

  case object ItTest extends TestType

  case object ItTestQuick extends TestType

  implicit def asCommand(dockerComposeTest: DockerComposeTest): DockerComposeTestCmd =
    DockerComposeTestCmd(dockerComposeTest)
}
