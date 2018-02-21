package com.github.ehsanyou.sbt.docker.compose.parsers

import com.github.ehsanyou.sbt.docker.compose.commands.down.DockerComposeDownCmd
import com.github.ehsanyou.sbt.docker.compose.commands.up.DockerComposeUpCmd
import org.scalatest.Matchers
import org.scalatest.WordSpec
import sbt.complete.Parser

class DockerComposeParserSpec extends WordSpec with Matchers {

  "DockerComposeParser" should {

    "parse up command" in {

      val Right((dockerCompose, command)) = Parser.parse(" up", dockerComposeParserImpl(Seq.empty))

      dockerCompose.isEmpty shouldEqual true
      command.name shouldEqual "up"
    }

    "parse down command" in {

      val Right((dockerCompose, command)) = Parser.parse(" down", dockerComposeParserImpl(Seq.empty))

      dockerCompose.isEmpty shouldEqual true
      command.name shouldEqual "down"
    }

    "not parse unknown command" in {

      val Left(err) = Parser.parse(" unknown", dockerComposeParserImpl(Seq.empty))

    }

    "parse docker compose -p, --no-color option along with up command and -d option" in {

      val Right((dockerCompose, command)) =
        Parser.parse(" --no-ansi -p veon up -d", dockerComposeParserImpl(Seq.empty))

      dockerCompose.options.size
      val Some(_) = dockerCompose.options.find(x => x.key == "-p" && x.value == Some("veon"))
      val Some(_) = dockerCompose.options.find(x => x.key == "--no-ansi" && x.value.isEmpty)

      command.name shouldEqual "up"

      command.asInstanceOf[DockerComposeUpCmd].isEmpty shouldEqual false
      val Some(_) =
        command.asInstanceOf[DockerComposeUpCmd].underlying.options.find(x => x.key == "-d" && x.value.isEmpty)

    }

    "parse docker compose -p, --no-color option along with down command and --rmi option" in {

      val Right((dockerCompose, command)) =
        Parser.parse(" --no-ansi -p veon down --rmi x", dockerComposeParserImpl(Seq.empty))

      dockerCompose.options.size
      val Some(_) = dockerCompose.options.find(x => x.key == "-p" && x.value == Some("veon"))
      val Some(_) = dockerCompose.options.find(x => x.key == "--no-ansi" && x.value.isEmpty)

      command.name shouldEqual "down"

      command.asInstanceOf[DockerComposeDownCmd].isEmpty shouldEqual false
      val Some(_) =
        command
          .asInstanceOf[DockerComposeDownCmd]
          .underlying
          .options
          .find(x => x.key == "--rmi" && x.value == Some("x"))

    }
  }
}
