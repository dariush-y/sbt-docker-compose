package com.ehsanyou.sbt.docker.compose.commands.dc

import com.ehsanyou.sbt.docker.compose.DataTypes.DockerComposeOption
import com.ehsanyou.sbt.docker.compose.DataTypes.DockerComposeOptionKey
import com.ehsanyou.sbt.docker.compose.commands.CommandCompanion

case class DockerCompose(options: Seq[DockerComposeOption] = Seq.empty)

object DockerCompose extends CommandCompanion {

  import DockerComposeOptionKey.{apply => ap}

  val nonNativeOptions: Seq[DockerComposeOptionKey] =
    ap("--project-name-suffix", keyOnly = false) ::
      Nil

  override val options: Seq[DockerComposeOptionKey] =
    ap(Seq("-f", "--file"), keyOnly = false) ::
      ap(Seq("-p", "--project-name"), keyOnly = false) ::
      ap("--verbose") ::
      ap("--no-ansi") ::
      ap(Seq("-H", "--host"), keyOnly = false) ::
      ap("--tls") ::
      ap("--tlscacert", keyOnly = false) ::
      ap("--tlscert", keyOnly = false) ::
      ap("--tlskey", keyOnly = false) ::
      ap("--tlsverify") ::
      ap("--skip-hostname-check") ::
      ap("--project-directory", keyOnly = false) ::
      Nil ++ nonNativeOptions

  implicit def asCommand(dockerCompose: DockerCompose): DockerComposeCmd = DockerComposeCmd(dockerCompose)
}
