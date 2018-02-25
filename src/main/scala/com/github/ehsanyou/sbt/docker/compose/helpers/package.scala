package com.github.ehsanyou.sbt.docker.compose

import com.github.ehsanyou.sbt.docker.compose.DataTypes.Cwd
import com.github.ehsanyou.sbt.docker.compose.DataTypes.InvalidExitCodeException
import sbt.Def
import sbt._

import scala.concurrent.Future
import scala.sys.process.Process

package object helpers {

  implicit def stateToExtracted(state: State): Extracted = Project.extract(state)

  object sbtFutureTask {
    def apply[T](t: T): Future[Def.Initialize[Task[T]]] = Future.successful(Def task t)
    def empty: Future[Def.Initialize[Task[Unit]]] = apply(())
  }

  object sbtTask {
    def empty: Def.Initialize[Task[Unit]] = Def task (())
  }

  def redPrinter(str: String): Unit = println(scala.Console.RED + str + scala.Console.WHITE)
  def greenPrinter(str: String): Unit = println(scala.Console.GREEN + str + scala.Console.WHITE)
  def yellowPrinter(str: String): Unit = println(scala.Console.YELLOW + str + scala.Console.WHITE)

  def process[T](
    command: String
  )(
    onSuccess: => T
  )(
    implicit cwd: Cwd
  ): T = {
    if (Process(command, cwd.dir).! == 0) onSuccess
    else throw new InvalidExitCodeException(s"`$command` command returned non-zero exit code.")
  }

  def processNonBlocking(
    command: String
  )(
    implicit cwd: Cwd
  ): Process =
    Process(command, cwd.dir).run()
}
