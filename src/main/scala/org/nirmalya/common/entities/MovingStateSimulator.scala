package org.nirmalya.common.entities

import akka.actor.{Actor, ActorLogging, FSM}

import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration

/**
  * Created by nirmalya on 28/10/16.
  */
trait MovingStateSimulator extends Actor {

  val timeToReachNextFloor: FiniteDuration = 1000 millis  // up  or down, same time taken
  def simulateMovementTo(fromFloorID: Int, nextStop: NextStop, durationToConsume: FiniteDuration): Unit

}

object DefaultMovingStateSimulatorActor extends MovingStateSimulator
  with FSM[LiftState, LiftData]
  with ActorLogging {

  startWith(Ready,InitialData)

  when (Ready) {
    case Event(InformMeOnReaching(from,nextStop),_) =>
      goto (Ready) replying(ReachedFloor(nextStop))
    case (x: Any)                                =>
      log.debug("Unknown message (" + x + ") received.")
      stay
  }
  def simulateMovementTo(fromFloorID: Int, nextStop: NextStop, durationToConsume: FiniteDuration) = {}
}