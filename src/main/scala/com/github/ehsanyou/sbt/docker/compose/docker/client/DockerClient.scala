package com.github.ehsanyou.sbt.docker.compose.docker.client

import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.DockerClient.ListContainersParam
import com.spotify.docker.client.messages.Container
import com.spotify.docker.client.messages.ContainerInfo
import com.spotify.docker.client.messages.ContainerState
import com.spotify.docker.client.messages.Info
import com.github.ehsanyou.sbt.docker.compose.docker.DataTypes
import com.github.ehsanyou.sbt.docker.compose.docker.DataTypes.PortMapping
import com.github.ehsanyou.sbt.docker.compose.docker.DataTypes.{ContainerInfo => CustomContainerInfo}

import scala.collection.JavaConverters._
import scala.util.Try

trait IDockerClient {

  def getContainers: Seq[Container]

  def inspectContainers: Seq[ContainerInfo]

  def containers: Seq[DataTypes.ContainerInfo]

  def inspectContainer(containerId: String): ContainerInfo

  def getHealthStatus(containerInfo: ContainerInfo): Option[ContainerState.Health]

  def getHost: String

  def info: Info

}

class DockerClient(
  projectName: String
) extends IDockerClient {

  private val defaultClient: DefaultDockerClient = {
    DefaultDockerClient.fromEnv().build()
  }

  override lazy val info: Info = defaultClient.info()

  override lazy val getHost: String = defaultClient.getHost

  override def getHealthStatus(containerInfo: ContainerInfo): Option[ContainerState.Health] =
    Try(Option(containerInfo.state().health())).toOption.flatten

  override def inspectContainer(containerId: String): ContainerInfo = defaultClient.inspectContainer(containerId)

  override def getContainers: Seq[Container] =
    defaultClient.listContainers(ListContainersParam.filter("name", projectName)).asScala

  override def inspectContainers: Seq[ContainerInfo] = getContainers map { container =>
    defaultClient.inspectContainer(container.id())
  }

  override def containers: Seq[CustomContainerInfo] = getContainers map { container =>
    CustomContainerInfo(
      id = container.id(),
      name = container.names().asScala.head,
      portMappings = container.ports().asScala map { portMapping =>
        PortMapping(
          privatePort = portMapping.privatePort(),
          publicPort = portMapping.publicPort(),
          ip = portMapping.ip()
        )
      }
    )
  }
}

object DockerClient {

  def normalizeContainerName(name: String): String = {
    if (name.startsWith("/") || name.startsWith("""\""")) {
      name.drop(1)
    } else name
  }.split("_").tail.mkString("_")

  def extractServiceName(containerName: String): String =
    normalizeContainerName(containerName).split("_").head
}
