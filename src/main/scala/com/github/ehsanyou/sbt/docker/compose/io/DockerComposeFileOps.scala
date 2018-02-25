package com.github.ehsanyou.sbt.docker.compose.io

import java.io.FileNotFoundException
import java.util.UUID

import cats.syntax.either._
import com.github.ehsanyou.sbt.docker.compose.io.DataTypes.ServiceName
import com.github.ehsanyou.sbt.docker.compose.io.DataTypes.ServiceWithTag
import io.circe.Json
import sbt.File
import sbt._

trait IDockerComposeFileOps {

  def store: File

  def getServices: Seq[ServiceName]

  def getServicesWithTag: Seq[ServiceWithTag]

  def withImageTags(tags: Seq[(String, String)]): IDockerComposeFileOps

}

case class DockerComposeFileOps(
  jsonAST: Json,
  cwd: File
) extends IDockerComposeFileOps {

  def asPrettyYaml: String = Printer.spaces2.pretty(jsonAST)

  override def getServices: Seq[ServiceName] =
    jsonAST.hcursor
      .downField("services")
      .fields
      .map(_.map(ServiceName).toSeq)
      .getOrElse(Seq.empty)

  override def getServicesWithTag: Seq[ServiceWithTag] = getServices flatMap { service =>
    jsonAST.hcursor
      .downField("services")
      .downField(service.name)
      .get[String]("image")
      .toOption
      .map { image =>
        val split = image.split(":").toSeq
        ServiceWithTag(service.name, split.drop(1).lastOption)
      }
  }

  def withImageTags(tags: Seq[(String, String)]): DockerComposeFileOps =
    tags.foldLeft(this) {
      case (acc, (serviceName, tag)) =>
        acc.replaceServiceTag(serviceName, tag) match {
          case Some(json) =>
            acc.copy(json)
          case None =>
            acc
        }
    }

  private def replaceServiceTag(serviceName: String, tag: String): Option[Json] =
    jsonAST.hcursor
      .downField("services")
      .downField(serviceName)
      .downField("image")
      .withFocus(_.mapString { image =>
        val split = image.split(":").toSeq
        s"${split.head}:$tag"
      })
      .top

  override def store: File = {
    val f: File = cwd / s"docker-compose-modified-${UUID.randomUUID()}.yml"
    sbt.IO.write(f, asPrettyYaml)
    f
  }
}

object DockerComposeFileOps {

  def apply(path: String, workingDir: File): DockerComposeFileOps =
    DcFileReader(path) match {
      case Right(json) =>
        DockerComposeFileOps(json, workingDir)
      case Left(err) =>
        throw new FileNotFoundException(err.msg)
    }
}
