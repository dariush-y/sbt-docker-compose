package com.ehsanyou.sbt.docker.compose.commands.up

import com.ehsanyou.sbt.docker.compose.DataTypes._
import com.ehsanyou.sbt.docker.compose.commands.CommandCompanion

case class DockerComposeUp(
  options: Seq[DockerComposeOption],
  services: Seq[DockerComposeOption],
  tags: Seq[(String, String)]
)

object DockerComposeUp extends CommandCompanion {

  import DockerComposeOptionKey.{apply => ap}

  override val options: Seq[DockerComposeOptionKey] =
    ap("-d") ::
      ap("--no-color") ::
      ap("--no-deps") ::
      ap("--force-recreate") ::
      ap("--no-recreate") ::
      ap("--no-build") ::
      ap("--build") ::
      ap("--abort-on-container-exit") ::
      ap("--remove-orphans") ::
      ap("--exit-code-from", keyOnly = false) ::
      ap("--scale", keyOnly = false) ::
      ap("--timeout", keyOnly = false) ::
      Nil

  def apply(): DockerComposeUp = DockerComposeUp(Seq.empty, Seq.empty, Seq.empty)

  implicit def asCommand(dockerComposeUp: DockerComposeUp): DockerComposeUpCmd =
    DockerComposeUpCmd(dockerComposeUp)
}
