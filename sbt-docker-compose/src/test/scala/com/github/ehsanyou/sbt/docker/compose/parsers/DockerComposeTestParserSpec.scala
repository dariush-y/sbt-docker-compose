package com.github.ehsanyou.sbt.docker.compose.parsers

import com.github.ehsanyou.sbt.docker.compose.commands.test.DockerComposeTest._
import org.scalatest.Matchers
import org.scalatest.WordSpec
import sbt.complete.Parser

class DockerComposeTestParserSpec extends WordSpec with Matchers {

  "DockerComposeTestParser" should {

    "parse it:test command" in {

      val Right((dockerCompose, dockerComposeTest)) = Parser.parse(" it:test", dockerComposeTestParserImpl(Seq.empty))

      dockerCompose.isEmpty shouldEqual true
      dockerComposeTest.underlying.testType shouldEqual ItTest
    }

    "parse it:testOnly command" in {

      val Right((dockerCompose, dockerComposeTest)) =
        Parser.parse(""" it:testOnly "*MyClass"""", dockerComposeTestParserImpl(Seq.empty))

      dockerCompose.isEmpty shouldEqual true
      dockerComposeTest.underlying.testType.asInstanceOf[ItTestOnly].arg shouldEqual "*MyClass"
    }

    "parse it:testQuick command" in {

      val Right((dockerCompose, dockerComposeTest)) =
        Parser.parse(" it:testQuick", dockerComposeTestParserImpl(Seq.empty))

      dockerCompose.isEmpty shouldEqual true
      dockerComposeTest.underlying.testType shouldEqual ItTestQuick
    }

    "parse test command" in {

      val Right((dockerCompose, dockerComposeTest)) = Parser.parse(" test", dockerComposeTestParserImpl(Seq.empty))

      dockerCompose.isEmpty shouldEqual true
      dockerComposeTest.underlying.testType shouldEqual Test
    }

    "parse testOnly command" in {

      val Right((dockerCompose, dockerComposeTest)) =
        Parser.parse(""" testOnly "*MyClass"""", dockerComposeTestParserImpl(Seq.empty))

      dockerCompose.isEmpty shouldEqual true
      dockerComposeTest.underlying.testType.asInstanceOf[TestOnly].arg shouldEqual "*MyClass"
    }

    "parse testQuick command" in {

      val Right((dockerCompose, dockerComposeTest)) =
        Parser.parse(" testQuick", dockerComposeTestParserImpl(Seq.empty))

      dockerCompose.isEmpty shouldEqual true
      dockerComposeTest.underlying.testType shouldEqual TestQuick
    }

    "support dockerCompose options i.e -p" in {

      val Right((dockerCompose, dockerComposeTest)) =
        Parser.parse(" -p veon test", dockerComposeTestParserImpl(Seq.empty))

      dockerComposeTest.underlying.testType shouldEqual Test
      dockerComposeTest.underlying.options shouldBe empty
      dockerCompose.options.find(x => x.key == "-p" && x.value == Some("veon"))

    }

    "support dockerCompose up options i.e -d" in {

      val Right((dockerCompose, dockerComposeTest)) =
        Parser.parse(" -p veon test", dockerComposeTestParserImpl(Seq.empty))

      dockerComposeTest.underlying.testType shouldEqual Test
      dockerComposeTest.underlying.options.find(x => x.key == "-d" && x.value.isEmpty)
    }
  }
}
