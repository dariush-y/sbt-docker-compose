package com.github.ehsanyou.sbt.docker.compose

import com.github.ehsanyou.sbt.docker.compose.DataTypes.DockerComposeOption
import com.github.ehsanyou.sbt.docker.compose.Keys.dockerComposeFilePath
import com.github.ehsanyou.sbt.docker.compose.Keys.dockerComposeIgnore
import com.github.ehsanyou.sbt.docker.compose.commands.dc.{DockerCompose => DockerComposeCommand}
import com.github.ehsanyou.sbt.docker.compose.commands.down.DockerComposeDown
import com.github.ehsanyou.sbt.docker.compose.commands.down.DockerComposeDownCmd
import com.github.ehsanyou.sbt.docker.compose.commands.Command
import com.github.ehsanyou.sbt.docker.compose.commands.test
import com.github.ehsanyou.sbt.docker.compose.commands.test.DockerComposeTest.ItTest
import com.github.ehsanyou.sbt.docker.compose.commands.test.DockerComposeTest.ItTestOnly
import com.github.ehsanyou.sbt.docker.compose.commands.test.DockerComposeTest.ItTestQuick
import com.github.ehsanyou.sbt.docker.compose.commands.test.DockerComposeTest.Test
import com.github.ehsanyou.sbt.docker.compose.commands.test.DockerComposeTest.TestOnly
import com.github.ehsanyou.sbt.docker.compose.commands.test.DockerComposeTest.TestQuick
import com.github.ehsanyou.sbt.docker.compose.commands.test.DockerComposeTest.TestType
import com.github.ehsanyou.sbt.docker.compose.commands.test.DockerComposeTestCmd
import com.github.ehsanyou.sbt.docker.compose.commands.test.{DockerComposeTest => DockerComposeTestDT}
import com.github.ehsanyou.sbt.docker.compose.commands.up.DockerComposeUp
import com.github.ehsanyou.sbt.docker.compose.commands.up.DockerComposeUpCmd
import com.github.ehsanyou.sbt.docker.compose.helpers._
import com.github.ehsanyou.sbt.docker.compose.io.DataTypes.ServiceWithTag
import com.github.ehsanyou.sbt.docker.compose.io.DockerComposeFileOps
import sbt.Keys.target
import sbt.Def
import sbt.Extracted
import sbt.State
import sbt.complete.DefaultParsers.Space
import sbt.complete.DefaultParsers.StringBasic
import sbt.complete.DefaultParsers.token
import sbt.complete.Parser

package object parsers extends ParserHelper {

  private val dockerComposeOptionParser: Parser[DockerComposeCommand] =
    optionParser(DockerComposeCommand.options) map (options => DockerComposeCommand(options))

  val dockerComposeParser: Def.Initialize[(State) => Parser[(DockerComposeCommand, Command)]] = Def.setting { state =>
    val extracted: Extracted = state
    val services =
      if (extracted.get(dockerComposeIgnore)) Seq.empty
      else DockerComposeFileOps(extracted.get(dockerComposeFilePath), extracted.get(target)).getServicesWithTag

    dockerComposeParserImpl(services)
  }

  def dockerComposeTestParser: Def.Initialize[(State) => Parser[(DockerComposeCommand, DockerComposeTestCmd)]] =
    Def.setting { state =>
      val extracted: Extracted = state
      val services =
        if (extracted.get(dockerComposeIgnore)) Seq.empty
        else DockerComposeFileOps(extracted.get(dockerComposeFilePath), extracted.get(target)).getServicesWithTag

      dockerComposeTestParserImpl(services)
    }

  def dockerComposeTestParserImpl(
    services: Seq[ServiceWithTag]
  ): Parser[(DockerComposeCommand, DockerComposeTestCmd)] =
    dockerComposeOptionParser ~ (
      Space ~> (sbtTestParser ~
        optionParser(test.DockerComposeTest.options).? ~
        serviceParser(services).? ~
        tagsParser(services).?)
    ) map {
      case (dc, (((testType, options), parsedServices), tags)) =>
        (
          dc,
          DockerComposeTestDT(
            testType = testType,
            options = options.getOrElse(Seq.empty),
            tags = tags.getOrElse(Seq.empty),
            services = parsedServices.getOrElse(Seq.empty),
            envVars = Seq.empty
          )
        )
    }

  def dockerComposeParserImpl(services: Seq[ServiceWithTag]): Parser[(DockerComposeCommand, Command)] =
    dockerComposeOptionParser ~ (
      Space ~> (
        dockerComposeUpParser(services) |
          dockerComposeDownParser(services)
      )
    )

  private def dockerComposeDownParser(services: Seq[ServiceWithTag]): Parser[DockerComposeDownCmd] = {
    (token("down") ~> optionParser(DockerComposeDown.options).? ~ serviceParser(services).?) map {
      case (options, parsedServices) =>
        DockerComposeDown(options.getOrElse(Seq.empty), parsedServices.getOrElse(Seq.empty))
    }
  }

  private def dockerComposeUpParser(services: Seq[ServiceWithTag]): Parser[DockerComposeUpCmd] =
    (token("up") ~> optionParser(DockerComposeUp.options).? ~ serviceParser(services).? ~ tagsParser(services).?) map {
      case ((options, parsedServices), tags) =>
        DockerComposeUp(
          options = options.getOrElse(Seq.empty),
          tags = tags.getOrElse(Seq.empty),
          services = parsedServices.getOrElse(Seq.empty),
          envVars = Seq.empty
        )
    }

  private def sbtTestParser: Parser[TestType] = {

    val testKey = "test"
    val testQuickKey = "testQuick"
    val testOnlyKey = "testOnly"
    val ItTestKey = "it:test"
    val ItTestQuickKey = "it:testQuick"
    val ItTestOnlyKey = "it:testOnly"

    (
      keyParser(testKey) |
        keyParser(testQuickKey) |
        kvParser(testOnlyKey, StringBasic) |
        keyParser(ItTestKey) |
        keyParser(ItTestQuickKey) |
        kvParser(ItTestOnlyKey, StringBasic)
    ) map { dockerComposeOption =>
      (dockerComposeOption: @unchecked) match {
        case DockerComposeOption(`testKey`, None) => Test
        case DockerComposeOption(`testQuickKey`, None) => TestQuick
        case DockerComposeOption(`testOnlyKey`, Some(v)) => TestOnly(v)
        case DockerComposeOption(`ItTestKey`, None) => ItTest
        case DockerComposeOption(`ItTestQuickKey`, None) => ItTestQuick
        case DockerComposeOption(`ItTestOnlyKey`, Some(v)) => ItTestOnly(v)
      }
    }
  }
}
