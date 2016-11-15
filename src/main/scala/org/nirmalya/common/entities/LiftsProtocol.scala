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

sealed trait IntraLiftMessage
case class   CurrentStateIndicatorMessage(info:  InfoForController) extends IntraLiftMessage
object       InstructedToPowerOn   extends IntraLiftMessage
object       InstructedToPowerOff  extends IntraLiftMessage
object       BeReady           extends IntraLiftMessage
case class   ReachedWaitingPassengerAt(floorID: Int)  extends IntraLiftMessage
case class   TransportingPassengerTo(floorID: Int) extends  IntraLiftMessage
case class   InstructedToDropPassengerAt(floorID: Int) extends  IntraLiftMessage
case class   StoppedAt(floorID: Int) extends  IntraLiftMessage
case class   PassengerIsWaitingAt(dest: Int) extends IntraLiftMessage
case class   ReachedFloor(thisStop: NextStop) extends IntraLiftMessage
case class   InformMeOnReaching(fromFloor: Int, toStop: NextStop)
case object  ReportCurrentFloor extends IntraLiftMessage

case class NextStop(floorID: Int, purposeOfMovement: PurposeOfMovement)

sealed trait UserAndLiftInteractionMessage
case class   PassengerRequestsATransportTo(dests: Vector[Int]) extends UserAndLiftInteractionMessage
case class   PassengerAskedForLiftAt(floorID: Int) extends UserAndLiftInteractionMessage

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