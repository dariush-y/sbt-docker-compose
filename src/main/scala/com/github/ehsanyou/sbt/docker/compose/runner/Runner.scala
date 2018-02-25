package com.github.ehsanyou.sbt.docker.compose.runner

import sbt.Def
import sbt.Task

import scala.concurrent.Future

trait Runner {
  def run: Future[Def.Initialize[Task[Unit]]]
}
