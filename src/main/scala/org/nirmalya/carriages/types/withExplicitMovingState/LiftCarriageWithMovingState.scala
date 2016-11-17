package org.nirmalya.carriages.types.withExplicitMovingState


import scala.concurrent.duration._
import akka.actor.{Actor, ActorLogging, ActorRef, FSM, LoggingFSM, Props}
import org.nirmalya.common.entities._

import scala.collection.immutable.Vector


/**
  * Created by nirmalya on 24/10/16.
  */
class LiftCarriageWithMovingState (val movementHWIndicator: ActorRef) extends Actor
  with LoggingFSM[LiftState,LiftData]
  with ActorLogging
{

  val mxTimeToWaitStopping = 250 milliseconds
  private var pendingPassengerRequests: Vector[NextStop] = Vector.empty
  private var currentFloorID = 0 // Always start at Ground Floor

  private val dontCare: StateFunction = {
    case _ =>
      log.debug(context.self.toString().+(StringContext.apply(" in ", ".stateName received message ( ", ".Event ), nothing to do.").s(this, this)))
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
      settleDownAtGroundFloor
      goto (Ready)
  }

  private val moveToWaitingPassenger: StateFunction = {
    case Event(PassengerIsWaitingAt(destFloorID), _) =>
      if (currentFloorID.==(destFloorID))
        goto (Stopped)
      else {

        this.pendingPassengerRequests = accumulateWaitingRequest(destFloorID)
        movementHWIndicator.!(InformMeOnReaching.apply(
                                  this.currentFloorID,
                                  this.pendingPassengerRequests.head))
        goto (Moving)
      }
  }

  private val transportPassengerToDest: StateFunction = {
    case Event(PassengerRequestsATransportTo(floorIDs),_) =>
      log.debug(StringContext.apply("Received message: PassengerWantsToGoTo(", ")").s(floorIDs))
      this.pendingPassengerRequests = accumulateTransportRequest(floorIDs)
      movementHWIndicator.!(InformMeOnReaching.apply(
                              this.currentFloorID,
                              this.pendingPassengerRequests.head))
      goto (Moving)
  }

  private val actWhenStoppedForLongEnough: StateFunction = {
    case Event(StateTimeout, _)   =>
      if (this.pendingPassengerRequests isEmpty) {
        log.debug("Stopped.timeout, No pending passenger requests")
        goto (Ready)
      }
      else {
        log.debug(StringContext.apply("Stopped.timeout, moving to floor:( ", " )").s(this.pendingPassengerRequests.head))
        movementHWIndicator.!(InformMeOnReaching.apply(
                                  this.currentFloorID,
                                  this.pendingPassengerRequests.head))
        goto(Moving)
      }
  }

  startWith(PoweredOff,InitialData)

  when (PoweredOff) (powerYourselfOn.orElse(dontCare))

  when (PoweredOn)  (powerYourselfOff orElse
                     beReady.orElse(dontCare))

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
      log.debug(StringContext.apply("Carriage(", "): reached(", "), remaining ", "").s(self.path.name, currentFloorID, prettifyForLogging(pendingPassengerRequests)))
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
      sender ! StoppedAt.apply(self.path.name,this.currentFloorID)
      stay
  }

  onTransition {
    case Stopped -> Stopped => {
      log.debug("Remaining in Stopped ...")
    }
  }

  private def settleDownAtGroundFloor = this.currentFloorID = 0

  private def accumulateWaitingRequest(toFloorID: Int): Vector[NextStop] =
    this.pendingPassengerRequests :+ NextStop.apply(toFloorID,PurposeOfMovement.ToWelcomeInAnWaitingPassenger)

  private def accumulateTransportRequest(toFloorIDs: Vector[Int]): Vector[NextStop] =
    this.pendingPassengerRequests ++
      (toFloorIDs.map(f => NextStop.apply(f,PurposeOfMovement.ToAllowATransportedPassengerAlight)))

  private def prettifyForLogging(floorsToVisit: Vector[NextStop]):String = {

    if (floorsToVisit.isEmpty)
      "(No more at the moment)"
    else
      floorsToVisit.foldLeft(new StringBuilder)((buffer,nextFloor) => {

        val toVisitFloor = nextFloor.floorID
        val reasonToVisitFloor =
          if (nextFloor.purposeOfMovement ==  PurposeOfMovement.ToAllowATransportedPassengerAlight)
            "toDrop"
          else "toPick"

        buffer.append(StringContext.apply("(", "-", ")").s(toVisitFloor, reasonToVisitFloor)).append(" | ")
      }).toString

  }

  initialize() // Customary initialization hook for an FSM
}

object LiftCarriageWithMovingState {
  def props(hwSimulator: ActorRef) = Props.apply(new LiftCarriageWithMovingState(hwSimulator))
}

