package org.nirmalya.common.entities

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging, ActorRef, FSM}

import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration

/**
  * Created by nirmalya on 28/10/16.
  */
abstract class MovingStateSimulator(hwID: Int) extends Actor with ActorLogging {

  case class SpentTimeToReach(nextStop: NextStop, carriageToBeInformed: ActorRef)

  val timeToReachNextFloor: FiniteDuration = 1000 millis  // up  or down, same time taken
  def simulateMovementTo(
       fromFloorID: Int,
       nextStop: NextStop): Unit

  override def receive: Receive = {
    case InformMeOnReaching(fromFloorID,nextStop) =>
      if (fromFloorID == nextStop.floorID) // Just a regular edge-case check
        sender ! ReachedFloor(nextStop)
      else
        simulateMovementTo(fromFloorID,nextStop)

    case SpentTimeToReach(nextStop,carriageToBeInformed)   =>
      log.debug(s"Informing ${carriageToBeInformed} after reaching the floor.")
      carriageToBeInformed ! ReachedFloor(nextStop)
  }
}

// Used for testing only
object DefaultMovingStateSimulatorActor extends MovingStateSimulator(0)
  with ActorLogging {

  override def simulateMovementTo(
                 fromFloorID: Int,
                 toNextStop: NextStop) = {

    // Immediate response, no time consumption in moving
    sender ! ReachedFloor(toNextStop)

  }
}