package com.ehsanyou.sbt.docker.compose.io

object DataTypes {

  case class ServiceName(name: String) extends AnyVal {
    override def toString: String = name
  }

  case class ServiceWithTag(name: String, tag: Option[String]) {
    override def toString: String = name + ":" + tag.getOrElse("?")
  }
}
