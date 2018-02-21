package com.ehsanyou.sbt.docker.compose

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.PoisonPill
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import com.spotify.docker.client.messages.ContainerInfo
import com.spotify.docker.client.messages.ContainerState
import com.ehsanyou.sbt.docker.compose.HealthCheckActor.Protocol.HealthCheck
import com.ehsanyou.sbt.docker.compose.HealthCheckActor.Protocol.Healthy
import com.ehsanyou.sbt.docker.compose.HealthCheckActor.Protocol.UnHealthy
import com.ehsanyou.sbt.docker.compose.docker.client.IDockerClient
import com.ehsanyou.sbt.docker.compose.helpers._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

class HealthCheckActor(
  containers: Seq[ContainerInfo],
  dockerClient: IDockerClient,
  deadline: FiniteDuration
) extends Actor {

  import HealthCheckActor.Protocol._

  implicit val ec: ExecutionContextExecutor = context.dispatcher

  override def receive: Receive = receiveImpl(containers, Seq.empty, Seq.empty)

  def receiveImpl(
    containersToCheck: Seq[ContainerInfo],
    containersWithUnknownHealthStatus: Seq[ContainerInfo],
    unHealthyContainers: Seq[ContainerInfo]
  ): Receive = {

    case HealthCheck =>
      val returnTo: ActorRef = sender()
      containersToCheck foreach { container =>
        self ! HealthCheckContainer(container, returnTo)
      }

      context.system.scheduler.scheduleOnce(deadline, self, ReturnTo(returnTo))

    case msg @ HealthCheckContainer(container, ref) =>
      val upToDateContainer: ContainerInfo = getUpToDateContainerInfo(container)
      dockerClient.getHealthStatus(upToDateContainer) match {
        case Some(health) =>
          if (isHealthy(health)) {

            val containers = containersToCheck.filterNot(_ == container)
            lazy val become: Unit = context.become(
              receiveImpl(containers, containersWithUnknownHealthStatus, unHealthyContainers)
            )

            containers.headOption match {
              case None =>
                become
                self ! ReturnTo(ref)
              case Some(_) =>
                become
            }

            printHealthy(container)

          } else if (isUnhealthy(health)) {
            context.become(
              receiveImpl(
                containersToCheck.filterNot(_ == container),
                containersWithUnknownHealthStatus,
                unHealthyContainers :+ container
              )
            )
            self ! ReturnTo(ref)
          } else {
            context.system.scheduler.scheduleOnce(500 millis, self, msg)
          }
        case None =>
          val containers = containersToCheck.filterNot(_ == container)

          context.become(
            receiveImpl(
              containers,
              containersWithUnknownHealthStatus :+ container,
              unHealthyContainers
            )
          )

          if (containers.isEmpty) {
            self ! ReturnTo(ref)
          }

          printUnknownHealthStatus(container)
      }

    case ReturnTo(returnee) =>
      (containersToCheck.headOption, unHealthyContainers.headOption) match {
        case (_, Some(_)) =>
          returnee ! Left[UnHealthy, Healthy](UnHealthy(containersToCheck, unHealthyContainers))
        case (Some(_), _) =>
          returnee ! Left[UnHealthy, Healthy](UnHealthy(containersToCheck, unHealthyContainers))
        case (None, _) =>
          returnee ! Right[UnHealthy, Healthy](Healthy)
      }

      self ! PoisonPill
  }

  private def getUpToDateContainerInfo(containerInfo: ContainerInfo): ContainerInfo =
    dockerClient.inspectContainer(containerInfo.id())

  private def printHealthy(containerInfo: ContainerInfo): Unit = greenPrinter(
    s"""`${containerInfo.name()}` with ID: `${containerInfo.id()}` is healthy!"""
  )

  private def printUnknownHealthStatus(containerInfo: ContainerInfo): Unit = yellowPrinter(
    s"""`${containerInfo.name()}` with ID: `${containerInfo.id()}` health status is unknown.""".stripMargin
  )

  private def isHealthy(health: ContainerState.Health): Boolean =
    health.status().contentEquals("healthy")

  private def isUnhealthy(health: ContainerState.Health): Boolean =
    health.status().contentEquals("unhealthy")
}

object HealthCheckActor {

  def healthCheck(
    containers: Seq[ContainerInfo],
    deadline: FiniteDuration
  )(
    implicit actorSystem: ActorSystem,
    dockerClient: IDockerClient
  ): Future[Either[UnHealthy, Healthy]] = {

    implicit val timeout: Timeout = deadline.plus(30 seconds)

    actorSystem.actorOf(
      Props(classOf[HealthCheckActor], containers, dockerClient, deadline)
    ) ? HealthCheck
  }.mapTo[Either[UnHealthy, Healthy]]

  object Protocol {

    sealed trait Healthy

    case object Healthy extends Healthy

    case object HealthCheck

    case class HealthCheckContainer(container: ContainerInfo, sender: ActorRef)

    case class ReturnTo(sender: ActorRef)

    case class UnHealthy(unknownContainers: Seq[ContainerInfo], unhealthyContainers: Seq[ContainerInfo])

  }
}
