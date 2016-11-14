package org.nirmalya.entities


import akka.actor.ActorLogging
import org.nirmalya.common.entities._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

/**
  * Created by nirmalya on 7/11/16.
  */
object TimeConsumingHWSignalSimulator extends MovingStateSimulator with ActorLogging {

  private def computeTimeToMoveBetweenFloors(fromFloorID: Int, toFloorID: Int): FiniteDuration = {

    this.timeToReachNextFloor * Math.abs(fromFloorID - toFloorID)
  }

  override def simulateMovementTo(fromFloorID: Int, nextStop: NextStop): Unit = {

    val evToFire = SpentTimeToReach(nextStop,sender)
    val direction = if (fromFloorID > nextStop.floorID) "Down" else "Up"
    context.system.scheduler.scheduleOnce(
      computeTimeToMoveBetweenFloors(fromFloorID,nextStop.floorID),
      self,
      evToFire
    )
  }
}
