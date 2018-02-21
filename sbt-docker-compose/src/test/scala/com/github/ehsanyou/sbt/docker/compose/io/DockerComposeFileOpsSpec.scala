package com.github.ehsanyou.sbt.docker.compose.io

import io.circe.parser._
import org.scalatest.Matchers
import org.scalatest.WordSpec

class DockerComposeFileOpsSpec extends WordSpec with Matchers {

  "#asYaml" should {
    "print Int values as int literal" in {

      val Right(json) = parse {
        """
          |{
          |  "a": 30
          |}
        """.stripMargin
      }

      val prettyYaml = new DockerComposeFileOps(json, null).asPrettyYaml
      prettyYaml should include("a: 30")
    }
  }

  "#getServicesWithTag" should {
    "return service name along with its tag" in {

      val Right(json) = parse {
        """
          |{
          |  "version" : "3",
          |  "services" :
          |   {
          |     "zookeeper": {
          |       "image" : "wurstmeister/zookeeper:2.1"
          |     }
          |   }
          |}
        """.stripMargin
      }

      val serviceWithTag = new DockerComposeFileOps(json, null).getServicesWithTag

      serviceWithTag.size shouldEqual 1

      serviceWithTag.head.name shouldEqual "zookeeper"
      serviceWithTag.head.tag.get shouldEqual "2.1"
    }
  }

  "#getServices" should {
    "return service name" in {

      val Right(json) = parse {
        """
          |{
          |  "version" : "3",
          |  "services" :
          |   {
          |     "zookeeper": {
          |       "image" : "wurstmeister/zookeeper:2.1"
          |     }
          |   }
          |}
        """.stripMargin
      }

      val services = new DockerComposeFileOps(json, null).getServices

      services.size shouldEqual 1

      services.head.name shouldEqual "zookeeper"
    }
  }

  "#withImageTags" should {
    "replace image tag" in {

      val Right(json) = parse {
        """
          |{
          |  "version" : "3",
          |  "services" :
          |   {
          |     "zookeeper": {
          |       "image" : "wurstmeister/zookeeper:2.1"
          |     }
          |   }
          |}
        """.stripMargin
      }

      val dcFileOps = new DockerComposeFileOps(json, null)

      val newFile = dcFileOps.withImageTags(Seq(("zookeeper", "3.0.0")))

      val serviceWithTag = newFile.getServicesWithTag

      serviceWithTag.size shouldEqual 1

      serviceWithTag.head.name shouldEqual "zookeeper"
      serviceWithTag.head.tag.get shouldEqual "3.0.0"
    }
  }
}
