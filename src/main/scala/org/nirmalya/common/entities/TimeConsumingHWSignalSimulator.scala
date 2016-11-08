package org.nirmalya.common.entities

import akka.actor.FSM

import scala.concurrent.duration.FiniteDuration

/**
  * Created by nirmalya on 7/11/16.
  */
object TimeConsumingHWSignalSimulator extends MovingStateSimulator with FSM[LiftState,LiftData] {

  override def simulateMovementTo(fromFloorID: Int, nextStop: NextStop, durationToConsume: FiniteDuration): Unit = {
    val evToFire = ReachedFloor(nextStop)
    val direction = if (fromFloorID > nextStop.floorID) "Down" else "Up"
    setTimer(
      s"Carriage-${System.currentTimeMillis}-ToFloor-$nextStop.floorID-$direction",
      evToFire,
      durationToConsume
    )
  }

}
