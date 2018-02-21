package com.github.ehsanyou.sbt.docker.compose.commands.down

import com.github.ehsanyou.sbt.docker.compose.DataTypes._
import com.github.ehsanyou.sbt.docker.compose.commands.CommandCompanion

case class DockerComposeDown(
  options: Seq[DockerComposeOption] = Seq.empty,
  services: Seq[DockerComposeOption] = Seq.empty
)

object DockerComposeDown extends CommandCompanion {

  import DockerComposeOptionKey.{apply => ap}

  override val options: Seq[DockerComposeOptionKey] =
    ap("--rmi", keyOnly = false) ::
      ap(Seq("-v", "--volumes")) ::
      ap("--remove-orphans") ::
      Nil

  implicit def asCommand(dockerComposeDown: DockerComposeDown): DockerComposeDownCmd =
    DockerComposeDownCmd(dockerComposeDown)
}
