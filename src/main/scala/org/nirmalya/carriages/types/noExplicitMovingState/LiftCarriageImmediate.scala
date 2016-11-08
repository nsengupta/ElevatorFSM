package org.nirmalya.carriages.types.noExplicitMovingState

import akka.actor.{Actor, ActorLogging, FSM}
import org.nirmalya.common.entities._
import org.nirmalya.entities._

/**
  * Created by nirmalya on 24/10/16.
  */
class LiftCarriageImmediate extends Actor
  with FSM[LiftState,LiftData]
  with ActorLogging
{

  val controllerActor = context.parent
  val defaultTimeTakenToReachAFloor = 10
  var pendingPassengerRequests: Vector[Int] = Vector.empty
  private var currentFloorID = 0 // Always start at Ground Floor

  private val dontCare: StateFunction = {
    case _ =>
      log.debug(context.self.toString() + s" in $this.stateName received message ( $this.Event ), nothing to do.")
      stay
  }

  private val powerYourselfOff: StateFunction = {
    case Event(InstructedToPowerOff,_) =>
      log.debug(context.self.toString()    + s" in $this.stateName received message (PowerYourselfoff).")
      stay
  }

  private val powerYourselfOn: StateFunction = {
    case Event(InstructedToPowerOn,_)  =>
      log.debug(context.self.toString()    + s" in $this.stateName received message (PowerYourselfOn)")
      goto (PoweredOn)
  }

  private val beReady: StateFunction = {
    case Event(BeReady,_)          =>
      log.debug(context.self.toString()  + s" in $this.stateName received message (BeReady)")
      settleDownAtGroundFloor
      sender ! StoppedAt(0)
      goto (Ready)
  }

  private val moveToWaitingPassenger: StateFunction = {
    case Event(PassengerIsWaitingAt(floorID), _) =>
      log.debug(context.self.toString() + s" in $this.stateName received message (PassengerIsWaiting ($floorID + ))")
      sender ! StoppedAt(floorID)
      goto (Stopped) // TODO: send information back to Controller here
  }

  private val transportPassengerToDest: StateFunction = {
    case Event(PassengerRequestsATransportTo(floorIDs),_) =>
      log.debug(context.self.toString() + " received message (" + "PassengerWantsToGoTo(" + floorIDs + ")" + ")")
      this.pendingPassengerRequests = this.pendingPassengerRequests ++ floorIDs // TODO: Sort in terms of floor
      sender ! TransportingPassengerTo(pendingPassengerRequests.head)
      goto (Stopped)
  }

  startWith(PoweredOff,InitialData)

  when (PoweredOff) (powerYourselfOn
                     orElse dontCare)

  when (PoweredOn)  (powerYourselfOff
                     orElse beReady
                     orElse dontCare)

  when (Ready)      (moveToWaitingPassenger
                     orElse transportPassengerToDest
                     orElse powerYourselfOff
                     orElse dontCare)

  when (Stopped)    (moveToWaitingPassenger
                     orElse transportPassengerToDest
                     orElse beReady
                     orElse dontCare)

  when (Moving)              {
    case Event(ReachedWaitingPassengerAt(floorID),_) =>
      log.debug(context.self.toString() + " received message (" + "ReachedPassengerAt(" + floorID + ")" + ")")
      sender ! InfoForController(self, LiftCarriageMovementAlert(Stopped,Some(floorID))) // TODO: Should this be done during the transition?
      goto (Stopped)
    case Event(InstructedToDropPassengerAt(floorID),_)                =>
      log.debug(context.self.toString() + " received message (" + "DroppingPassengerAt(" + floorID + ")" + ")")
      this.pendingPassengerRequests = this.pendingPassengerRequests.tail
      if (this.pendingPassengerRequests.isEmpty) {
        sender ! InfoForController(self, LiftCarriageMovementAlert(Stopped,Some(floorID)))
        goto (Stopped)
      }
      else
        stay
    case Event(x,_)                                  =>
      log.debug(context.self.toString() + " received (unknwon) message (" + x + ")")
      stay
  }

  whenUnhandled {
    case Event(ReportCurrentFloor,_) => {
      sender ! this.currentFloorID
      stay
    }
  }

  private def settleDownAtGroundFloor = this.currentFloorID = 0

}
