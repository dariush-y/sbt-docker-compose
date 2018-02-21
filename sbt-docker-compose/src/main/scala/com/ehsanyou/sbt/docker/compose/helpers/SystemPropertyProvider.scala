package com.ehsanyou.sbt.docker.compose.helpers

import com.ehsanyou.sbt.docker.compose.docker.DataTypes.ContainerInfo
import com.ehsanyou.sbt.docker.compose.docker.client.DockerClient
import com.ehsanyou.sbt.docker.compose.docker.client.IDockerClient

object SystemPropertyProvider {

  def apply(containers: Seq[ContainerInfo])(implicit dockerClient: IDockerClient): Unit =
    containers
      .flatMap(containerNameMapper(_, dockerClient.getHost))
      .map {
        case (k, v) =>
          System.setProperty(k, v)
      }

  def containerNameMapper(container: ContainerInfo, hostName: String): Seq[(String, String)] = {

    val normalizedName = DockerClient.normalizeContainerName(container.name)
    val serviceName = DockerClient.extractServiceName(container.name)

    container.portMappings flatMap { mapping =>
      (s"${normalizedName}_${mapping.privatePort}", s"$hostName:${mapping.publicPort}") ::
        (s"${normalizedName}_${mapping.privatePort}_host", s"$hostName") ::
        (s"${normalizedName}_${mapping.privatePort}_port", s"${mapping.publicPort}") ::
        (s"${serviceName}_host", s"$hostName") ::
        (s"${serviceName}_port", s"${mapping.publicPort}") ::
        (s"$serviceName", s"$hostName:${mapping.publicPort}") ::
        Nil
    }
  }
}
