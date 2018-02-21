package com.ehsanyou.sbt.docker.compose

import com.ehsanyou.sbt.docker.compose.commands.dc.DockerComposeCmd
import com.ehsanyou.sbt.docker.compose.commands.down.DockerComposeDownCmd
import com.ehsanyou.sbt.docker.compose.commands.test.DockerComposeTestCmd
import com.ehsanyou.sbt.docker.compose.commands.up.DockerComposeUpCmd
import sbt._

import scala.concurrent.duration.FiniteDuration

object Keys {

  val dockerCompose = inputKey[Unit]("docker-compose command wrapper")

  val dockerComposeProjectName =
    settingKey[String]("Value returned by this setting maps to docker-compose `-p` option.")

  val dockerComposeCommandOptions = taskKey[DockerComposeCmd]("Default options for `docker-compose` command")

  val dockerComposeUpCommandOptions = taskKey[DockerComposeUpCmd]("Default options for `docker-compose up` command")

  val dockerComposeDownCommandOptions =
    taskKey[DockerComposeDownCmd]("Default options for `docker-compose down` command")

  val dockerComposeTags = settingKey[Seq[(String, String)]](
    "List of services and and their respective tag you'd like to override, --tags option in cli will supersede tags defined in this setting key"
  )

  val dockerComposeFilePath = settingKey[String]("docker-compose file path")

  val dockerComposeIgnore = settingKey[Boolean](
    "Ignores all provided tasks in project scope -- useful for root projects in multi-project configuration"
  )

  val dockerComposeTest = inputKey[Unit]("`docker-compose up` -> health check -> test -> `docker-compose down`")

  val dockerComposeTestQuick = inputKey[Unit]("`docker-compose up` -> health check -> test")

  val dockerComposeHealthCheckDeadline = taskKey[FiniteDuration]("Overall deadline for health-checking")

  val dockerComposeTestCommandOptions = taskKey[DockerComposeTestCmd](
    "Since `dockerComposeTest` task uses `docker-compose up` behind the scene, you can configure default options for it"
  )
  val dockerComposeTestDummy = taskKey[Unit]("A dummy task to check if sbt docker compose is available or not")

  val dockerComposeTestLogging =
    settingKey[Boolean]("A setting flag to enable disable container logging in dockerComposeTest task")

}
