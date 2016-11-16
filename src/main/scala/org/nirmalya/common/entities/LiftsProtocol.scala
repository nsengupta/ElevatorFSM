package org.nirmalya.common.entities

import akka.actor.ActorRef
import org.nirmalya.common.entities.PurposeOfMovement.PurposeOfMovement

/**
  * Created by nirmalya on 20/10/16.
  */



case class   LiftCarriageMovementAlert(
                                        liftState: LiftState,
                                        nextFloor: Option[Int] = None
                                      )
case class   InfoForController(liftID: ActorRef, customaryAlert: LiftCarriageMovementAlert)

object PurposeOfMovement extends Enumeration {
  type PurposeOfMovement = Value
  val ToWelcomeInAnWaitingPassenger, ToAllowATransportedPassengerAlight = Value
}

sealed trait ControllerAndCarriageInteractionMessage
case class   CurrentStateIndicatorMessage(info:  InfoForController) extends ControllerAndCarriageInteractionMessage
object       InstructedToPowerOn   extends ControllerAndCarriageInteractionMessage
object       InstructedToPowerOff  extends ControllerAndCarriageInteractionMessage
object       BeReady           extends ControllerAndCarriageInteractionMessage
case class   ReachedWaitingPassengerAt(floorID: Int)  extends ControllerAndCarriageInteractionMessage
case class   TransportingPassengerTo(floorID: Int) extends  ControllerAndCarriageInteractionMessage
case class   StoppedAt(name: String = "NA", floorID: Int) extends  ControllerAndCarriageInteractionMessage
case class   PassengerIsWaitingAt(dest: Int) extends ControllerAndCarriageInteractionMessage
case class   ReachedFloor(thisStop: NextStop) extends ControllerAndCarriageInteractionMessage
case class   InformMeOnReaching(fromFloor: Int, toStop: NextStop)
case object  ReportCurrentFloor extends ControllerAndCarriageInteractionMessage
case class   InquireWithCarriage(carriageID: Int) extends ControllerAndCarriageInteractionMessage

case class NextStop(floorID: Int, purposeOfMovement: PurposeOfMovement)

sealed trait PassengerAndCarriageInteractionMessage
case class   PassengerRequestsATransportTo(dests: Vector[Int]) extends PassengerAndCarriageInteractionMessage
case class   PassengerAskedForLiftAt(floorID: Int) extends PassengerAndCarriageInteractionMessage

sealed trait LiftState
object PoweredOff extends LiftState
object PoweredOn  extends LiftState
object Ready      extends LiftState
object Waiting    extends LiftState
object Moving     extends LiftState
object Stopped    extends LiftState


sealed trait      LiftData
object InitialData extends LiftData
case class PassengerIsAt(floorID: Int) extends LiftData
case class RemainingFloorsToGoto(floors: Vector[NextStop]) extends LiftData