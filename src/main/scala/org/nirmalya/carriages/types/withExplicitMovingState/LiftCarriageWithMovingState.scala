package org.nirmalya.carriages.types.withExplicitMovingState


import scala.concurrent.duration._
import akka.actor.{Actor, ActorLogging, ActorRef, FSM, LoggingFSM, Props}
import org.nirmalya.common.entities._

import scala.collection.immutable.Vector


/**
  * Created by nirmalya on 24/10/16.
  */
class LiftCarriageWithMovingState (val movementHWIndicator: Option[ActorRef] = None) extends Actor
  with LoggingFSM[LiftState,LiftData]
  with ActorLogging
{

  import context._

  val mxTimeToWaitStopping = 250 milliseconds
  private var pendingPassengerRequests: Vector[NextStop] = Vector.empty
  private var currentFloorID = 0 // Always start at Ground Floor

  private val dontCare: StateFunction = {
    case _ =>
      log.debug(context.self.toString() + s" in $this.stateName received message ( $this.Event ), nothing to do.")
      stay
  }

  private val powerYourselfOff: StateFunction = {
    case Event(InstructedToPowerOff,_) =>
      stay
  }

  private val powerYourselfOn: StateFunction = {
    case Event(InstructedToPowerOn,_)  =>
      goto (PoweredOn)
  }

  private val beReady: StateFunction = {
    case Event(BeReady,_)          =>
      log.debug(context.self.toString()  + s" in $this.stateName received message (BeReady)")
      settleDownAtGroundFloor
      goto (Ready)
  }

  private val moveToWaitingPassenger: StateFunction = {
    case Event(PassengerIsWaitingAt(destFloorID), _) =>
      //log.debug(context.self.toString() + s" in $this.stateName received message (PassengerIsWaiting ($destFloorID + ))")
      if (currentFloorID == destFloorID)
        goto (Stopped)
      else {

        this.pendingPassengerRequests = accumulateWaitingRequest(destFloorID)
        simulateMovementTo(this.pendingPassengerRequests.head)
        goto (Moving)
      }
  }

  private val transportPassengerToDest: StateFunction = {
    case Event(PassengerRequestsATransportTo(floorIDs),_) =>
      log.debug(s"Received message: PassengerWantsToGoTo($floorIDs)")
      this.pendingPassengerRequests = accumulateTransportRequest(floorIDs)
      simulateMovementTo(this.pendingPassengerRequests.head)
      goto (Moving)
  }

  private val actWhenStoppedForLongEnough: StateFunction = {
    case Event(StateTimeout, _)   =>
      if (this.pendingPassengerRequests isEmpty) {
        log.debug("Stopped.timeout, No pending passenger requests")
        goto (Ready)
      }
      else {
        log.debug(s"Stopped.timeout, moving to floor:( ${this.pendingPassengerRequests.head} )")
        simulateMovementTo(this.pendingPassengerRequests.head)
        goto(Moving)
      }
  }

  startWith(PoweredOff,InitialData)

  when (PoweredOff) (powerYourselfOn orElse
                     dontCare)

  when (PoweredOn)  (powerYourselfOff orElse
                     beReady orElse
                     dontCare)

  when (Ready)      (moveToWaitingPassenger   orElse
                     transportPassengerToDest )


  when (Stopped, 1 seconds) (actWhenStoppedForLongEnough orElse
                             moveToWaitingPassenger      orElse
                             transportPassengerToDest    )

  when (Moving)              {
    case Event(ReachedFloor(currentStop),_) =>
      currentFloorID = pendingPassengerRequests.head.floorID

      // All pending requests to this floor, should be removed.
      pendingPassengerRequests = pendingPassengerRequests.tail filter (n => n.floorID != this.currentFloorID)
      log.debug(s"current Floor ($currentFloorID), remaining to go ($pendingPassengerRequests)")
      goto (Stopped)

    case Event(PassengerIsWaitingAt(floorID),_)      =>
         this.pendingPassengerRequests = accumulateWaitingRequest(floorID)
         stay

    case Event(PassengerRequestsATransportTo(floorIDs),_)                =>
      this.pendingPassengerRequests = accumulateTransportRequest(floorIDs)
      stay
  }

  whenUnhandled {
    case Event(ReportCurrentFloor, _) =>
      //log.debug(s" In $stateName, Unhandled message ReportCurrentFloor received, currently at floor ${this.currentFloorID}")
      sender ! StoppedAt(this.currentFloorID)
      stay
  }

  onTransition {
    case Stopped -> Stopped => {
      println("Remaining in Stopped ...")
    }
  }

  private def simulateMovementTo(nextStop: NextStop) = {
    log.debug(s"Simulating movment to ${nextStop.floorID},${nextStop.purposeOfMovement}")

    this.movementHWIndicator match {
       case Some(handler) => handler ! InformMeOnReaching(this.currentFloorID, nextStop)
       case None          => self ! ReachedFloor(nextStop)
     }

  }

  private def settleDownAtGroundFloor = this.currentFloorID = 0

  private def accumulateWaitingRequest(toFloorID: Int): Vector[NextStop] =
    this.pendingPassengerRequests :+ NextStop(toFloorID,PurposeOfMovement.ToWelcomeInAnWaitingPassenger)

  private def accumulateTransportRequest(toFloorIDs: Vector[Int]): Vector[NextStop] =
    this.pendingPassengerRequests ++
      (toFloorIDs.map(f => NextStop(f,PurposeOfMovement.ToAllowATransportedPassengerAlight)))

  initialize() // Customary initialization hook for an FSM


}

