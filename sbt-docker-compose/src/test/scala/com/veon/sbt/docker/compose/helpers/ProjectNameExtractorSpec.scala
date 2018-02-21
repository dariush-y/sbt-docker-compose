package com.veon.sbt.docker.compose.helpers

import com.veon.sbt.docker.compose.DataTypes.DockerComposeOption
import org.scalatest.Matchers
import org.scalatest.WordSpec

class ProjectNameExtractorSpec extends WordSpec with Matchers {

  "ProjectNameExtractor" should {

    "extract default project name" in {

      val defaultProjectName = "default"
      val defaultUserDefinedOptions = Seq.empty[DockerComposeOption]
      val userDefinedOptions = Seq.empty[DockerComposeOption]
      val Some(projectName) =
        ProjectNameExtractor(defaultProjectName, defaultUserDefinedOptions, userDefinedOptions).value
      projectName shouldEqual defaultProjectName

    }

    "extract default user defined project name" in {

      val defaultProjectName = "default"
      val defaultUserDefinedOptions = Seq(DockerComposeOption("-p", "default2"))
      val userDefinedOptions = Seq.empty[DockerComposeOption]
      val Some(projectName) =
        ProjectNameExtractor(defaultProjectName, defaultUserDefinedOptions, userDefinedOptions).value
      projectName shouldEqual "default2"

    }

    "extract user defined project name" in {

      val defaultProjectName = "default"
      val defaultUserDefinedOptions = Seq.empty[DockerComposeOption]
      val userDefinedOptions = Seq(DockerComposeOption("-p", "default3"))
      val Some(projectName) =
        ProjectNameExtractor(defaultProjectName, defaultUserDefinedOptions, userDefinedOptions).value
      projectName shouldEqual "default3"

    }

  }

}
