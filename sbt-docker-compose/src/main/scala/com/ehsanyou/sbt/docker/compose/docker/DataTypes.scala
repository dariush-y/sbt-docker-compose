package com.ehsanyou.sbt.docker.compose.docker

object DataTypes {

  case class PortMapping(
    privatePort: Int,
    publicPort: Int,
    ip: String
  )

  case class ContainerInfo(id: String, name: String, portMappings: Seq[PortMapping])

}