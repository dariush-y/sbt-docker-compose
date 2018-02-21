package com.ehsanyou.sbt.docker.compose.parsers

import com.ehsanyou.sbt.docker.compose.DataTypes._
import com.ehsanyou.sbt.docker.compose.io.DataTypes.ServiceWithTag
import sbt.complete.DefaultParsers.NotSpace
import sbt.complete.DefaultParsers.Space
import sbt.complete.DefaultParsers.token
import sbt.complete.DefaultParsers._
import sbt.complete.Parser

trait ParserHelper {

  def tagsParser(services: Seq[ServiceWithTag]): Parser[Seq[(String, String)]] =
    services.headOption match {
      case Some(_) =>
        (Space ~>
          token("--tags") ~>
          Space ~>
          services
            .map(
              s =>
                token(s"${s.name}") ~
                  (token(":") ~> token(NotSpace, s.tag.getOrElse("?")))
            )
            .reduceLeft(_ | _)).*
      case None =>
        Parser.success(Seq.empty[(String, String)])
    }

  def serviceParser(services: Seq[ServiceWithTag]): Parser[Seq[DockerComposeOption]] =
    services.headOption match {
      case Some(_) =>
        (Space ~> services.map(s => token(s.name)).reduceLeft(_ | _)).* map (_.map(k => DockerComposeOption(k, None)))
      case None =>
        Parser.success(Seq.empty[DockerComposeOption])
    }

  def keyParser(key: String): Parser[DockerComposeOption] = token(key) map (k => DockerComposeOption(k, None))

  def keyParser(keys: Seq[String]): Seq[Parser[DockerComposeOption]] = keys map keyParser

  def kvParser(keys: Seq[String]): Seq[Parser[DockerComposeOption]] = keys map (a => kvParser(a))

  def kvParser(key: String, valueParser: Parser[String] = NotSpace): Parser[DockerComposeOption] =
    token(key) ~ (Space ~> valueParser) map {
      case (k, v) =>
        DockerComposeOption(k, Some(v))
    }

  def optionParser(values: Seq[DockerComposeOptionKey]): Parser[Seq[DockerComposeOption]] = {

    val parsers = values flatMap { x =>
      if (x.keyOnly) keyParser(x.keys)
      else kvParser(x.keys)
    }

    (Space ~> parsers.reduceLeft(_ | _)).*
  }
}
