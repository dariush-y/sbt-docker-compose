package com.github.ehsanyou.sbt.docker.compose.docker.client

import com.spotify.docker.client.messages.Info
import org.scalatest.Matchers
import org.scalatest.WordSpec

class DockerClientSpec extends WordSpec with Matchers {

  "DockerClient" should {
    "return the list of containers" in {
      val client = new DockerClient("dummy")
      val dockerInfo: Info = client.info
      dockerInfo.containers().intValue() shouldNot equal(-1)
    }
  }

  "normalizeContainerName" should {

    "remove project name from container name" in {
      DockerClient.normalizeContainerName("projectname_servicename_hostport_scalenumber") shouldEqual "servicename_hostport_scalenumber"
    }

    "remove / or \\ if it is container name's first character" in {
      DockerClient.normalizeContainerName("""\projectname_servicename_hostport_scalenumber""") shouldEqual "servicename_hostport_scalenumber"
      DockerClient.normalizeContainerName("""/projectname_servicename_hostport_scalenumber""") shouldEqual "servicename_hostport_scalenumber"
    }

    "remove project name and / \\ if it is container name's first character" in {
      DockerClient.normalizeContainerName("""\projectname_servicename_hostport_scalenumber""") shouldEqual "servicename_hostport_scalenumber"
      DockerClient.normalizeContainerName("""/projectname_servicename_hostport_scalenumber""") shouldEqual "servicename_hostport_scalenumber"
    }
  }

  "extractServiceName" should {

    "remove scale number from container name" in {
      DockerClient.extractServiceName("projectname_servicename_hostport_scalenumber") shouldEqual "servicename"
    }
  }
}
