package org.nirmalya.entities

import akka.actor.{Actor, ActorLogging, ActorRef, FSM}
import org.nirmalya.common.entities._
import org.nirmalya.entities

/**
  * Created by nirmalya on 20/10/16.
  */
class LiftController (id: Int, carriages: Vector[ActorRef]) extends Actor with FSM[LiftState,LiftData] with ActorLogging {

  import context._

  val circularArrangementOfCarriages = Iterator.continually(carriages).flatten

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
      goto (Ready)
  }

  private val respondToWaitingPassenger: StateFunction = {
    case Event(PassengerAskedForLiftAt(floorID),_)          =>
      log.debug(context.self.toString()  + s" in $this.stateName received message (BeReady)")
      chooseCarriageRR ! PassengerIsWaitingAt(floorID)
      stay
  }


  startWith(PoweredOff,InitialData)

  when (PoweredOff) (powerYourselfOn
                     orElse dontCare)

  when (PoweredOn)  (powerYourselfOff
                     orElse beReady
                     orElse dontCare)

  when (Ready)      (respondToWaitingPassenger
                     orElse powerYourselfOff
                     orElse dontCare)


  private def chooseCarriageRR: ActorRef = {
     // A simple way to implement a RoundRobin (RR) selection of a LiftCarriage
     this.circularArrangementOfCarriages.next
  }

}
