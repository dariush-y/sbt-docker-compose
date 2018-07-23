package com.github.ehsanyou.sbt.docker.compose

import java.io.File

object DataTypes {

  class InvalidExitCodeException(val message: String) extends Exception(message)

  case class Cwd(dir: File) extends AnyVal

  case class DockerComposeOptionKey(keys: Seq[String], keyOnly: Boolean)
  object DockerComposeOptionKey {
    def apply(keys: Seq[String]): DockerComposeOptionKey = apply(keys, keyOnly = true)
    def apply(key: String, keyOnly: Boolean): DockerComposeOptionKey = apply(Seq(key), keyOnly)
    def apply(key: String): DockerComposeOptionKey = apply(Seq(key), keyOnly = true)
  }

  case class DockerComposeOption(key: String, value: Option[String]) {
    def asStringSeq: Seq[String] = value match {
      case Some(v) => Seq(key, v)
      case _ => Seq(key)
    }
  }
  object DockerComposeOption {
    def apply(key: String): DockerComposeOption = apply(key, None)
    def apply(key: String, value: String): DockerComposeOption = apply(key, Some(value))
  }

  sealed trait DockerComposeError {
    def msg: String
  }

  case class GeneralError(override val msg: String) extends DockerComposeError

  case class FileNotFound(path: String) extends DockerComposeError {
    override val msg: String = path + " -- not found!"
  }
}
