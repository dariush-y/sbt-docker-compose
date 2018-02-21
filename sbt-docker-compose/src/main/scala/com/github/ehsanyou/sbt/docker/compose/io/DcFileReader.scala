package com.github.ehsanyou.sbt.docker.compose.io

import com.github.ehsanyou.sbt.docker.compose.DataTypes.DockerComposeError
import com.github.ehsanyou.sbt.docker.compose.DataTypes.FileNotFound
import com.github.ehsanyou.sbt.docker.compose.DataTypes.GeneralError
import io.circe.Json
import io.circe.yaml

import sbt.File
import sbt.IO

object DcFileReader {

  /**
    * Takes path to `docker-compose.yml`
    * , and then parses it to Json
    * @param path
    * @return
    */
  def apply(path: String): Either[DockerComposeError, Json] = {

    val file = new File(path)

    {
      if (file.exists()) Right(file)
      else Left(FileNotFound(path))
    }.right flatMap apply
  }

  def apply(file: File): Either[DockerComposeError, Json] =
    yaml.parser.parse(IO.read(file)).left.map(e => GeneralError(e.message))
}
