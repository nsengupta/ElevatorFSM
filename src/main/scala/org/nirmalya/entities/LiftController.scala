package org.nirmalya.entities

import akka.actor.{Actor, ActorLogging, ActorRef, LoggingFSM, Props}
import org.nirmalya.common.entities._


/**
  * Created by nirmalya on 20/10/16.
  */
class LiftController (carriages: Vector[ActorRef]) extends Actor
  with LoggingFSM[LiftState,LiftData]
  with ActorLogging {

  import context._

  val circularArrangementOfCarriages = Iterator.continually(carriages).flatten

  private val dontCare: StateFunction = {
    case _ =>
      log.debug(s" in $this.stateName received message ( $this.Event ), nothing to do.")
      stay
  }

  private val powerYourselfOff: StateFunction = {
    case Event(InstructedToPowerOff, _) =>
      log.debug(s"Controller:  in $this.stateName received message (PowerYourselfoff).")
      stay
  }

  private val powerYourselfOn: StateFunction = {
    case Event(InstructedToPowerOn, _) => goto(PoweredOn)
  }

  private val beReady: StateFunction = {
    case Event(BeReady, _) => goto(Ready)
  }

  private val respondToWaitingPassenger: StateFunction = {
    case Event(PassengerIsWaitingAt(floorID), _) =>
      val carriageChosen = chooseCarriageRR
      log.debug(s"Controller: instructing Carriage(${carriageChosen.path.name}), to pick passenger at ($floorID).")
      carriageChosen ! PassengerIsWaitingAt(floorID)
      stay
  }

  private val inquireCarriage: StateFunction = {
    case Event(InquireWithCarriage(carriageID), _) =>
      log.debug(s"Controller: asking Carriage(${carriages(carriageID).path.name}), its current floor.")
      carriages(carriageID) ! ReportCurrentFloor
      stay
    case Event(StoppedAt(name,floorID),_)  =>
      log.debug(s"Controller: Carriage(${name}), is at floor($floorID), at the moment.")
      stay
  }

  onTransition {
      case PoweredOff -> PoweredOn   => this.carriages.foreach( nextCarriage => nextCarriage ! InstructedToPowerOn)
      case PoweredOn  -> PoweredOff  => this.carriages.foreach( nextCarriage => nextCarriage ! InstructedToPowerOff)
      case PoweredOn  -> Ready       => this.carriages.foreach( nextCarriage => nextCarriage ! BeReady)
  }

  startWith(PoweredOff,InitialData)

  when (PoweredOff) (powerYourselfOn
                     orElse dontCare)

  when (PoweredOn)  (powerYourselfOff
                     orElse beReady
                     orElse dontCare)

  when (Ready)      (respondToWaitingPassenger
                     orElse inquireCarriage
                     orElse powerYourselfOff
                     orElse dontCare)


  private def chooseCarriageRR: ActorRef = {
     // A simple way to implement a RoundRobin (RR) selection of a LiftCarriage
     this.circularArrangementOfCarriages.next
  }

  // Mandatory initialization of the FSM
  initialize

}

// Recommended factory method
object LiftController {
  def props(carriages: Vector[ActorRef]) = Props(new LiftController(carriages))
}
