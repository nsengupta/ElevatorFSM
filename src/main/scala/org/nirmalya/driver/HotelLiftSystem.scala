package org.nirmalya.driver

import akka.actor.{ActorRef, ActorSystem}
import org.nirmalya.carriages.types.withExplicitMovingState.LiftCarriageWithMovingState
import org.nirmalya.entities.{InlaidButtonPanel, LiftController, TimeConsumingHWSignalSimulator}
import org.nirmalya.common.entities
import org.nirmalya.common.entities._

/**
  * Created by nirmalya on 15/11/16.
  */
object HotelLiftSystem extends App {

  implicit val system = ActorSystem()

  case class Lift (carriage: ActorRef, buttonPanel: ActorRef, hwSimulator: ActorRef)

  val lifts = (0 to 1) map {i =>

    val hwSignalSimulatorProps = TimeConsumingHWSignalSimulator.props(i)
    val hwSignalSimulator      = system.actorOf(hwSignalSimulatorProps,s"hw_$i")
    // Carriage needs a HW Simulator
    val carriage               = system.actorOf(LiftCarriageWithMovingState.props(hwSignalSimulator), s"Carriage_$i")

    // Buttonpanel needs a Carriage to be attached to
    val buttonPanel            = system.actorOf(InlaidButtonPanel.props(i,carriage),s"ButtonPanel_$i")

    Lift(carriage,buttonPanel,hwSignalSimulator)
  }

  val controller = system.actorOf(LiftController.props(lifts.map(l => l.carriage).toVector),"Controller")

  // Hotel's Office staff powers the Controller on.
  controller ! InstructedToPowerOn

  // Thread.sleep(1000)

  controller ! BeReady

  // A passenger at the 2nd floor, presses the button at the lift-lobby.
  controller ! PassengerIsWaitingAt(2)

  // Another passenger at the 2nd floor, presses the button at the lift-lobby.
  controller ! PassengerIsWaitingAt(4)

  // More of a debugging aid: Controller confirms where the carriage is right now.
  controller  ! InquireWithCarriage(1)

  // Let the threads in the dispatcher execute various Actors (all asynchronous).
  Thread.sleep(3000)

  // A passenger inside the Carriage(0), wants to go to 5th floor.
  lifts(0).buttonPanel ! PassengerRequestsATransportTo(Vector(5))

  // Two passengers inide the Carriage(1), want to go to 6th and 2nd floor.
  lifts(1).buttonPanel ! PassengerRequestsATransportTo(Vector(6,2))

  Thread.sleep(3000)

  // More of a debugging aid: Controller confirms where the carriage is right now.
  controller  ! InquireWithCarriage(0)

  // More of a debugging aid: Controller confirms where the carriage is right now.
  controller  ! InquireWithCarriage(1)

  Thread.sleep(3000)

  // More of a debugging aid: Controller confirms where the carriage is right now.
  controller  ! InquireWithCarriage(0)

  // More of a debugging aid: Controller confirms where the carriage is right now.
  controller  ! InquireWithCarriage(1)

  Thread.sleep(3000)

  // More of a debugging aid: Controller confirms where the carriage is right now.
  controller  ! InquireWithCarriage(0)

  // More of a debugging aid: Controller confirms where the carriage is right now.
  controller  ! InquireWithCarriage(1)

  Thread.sleep(2000)

  // More of a debugging aid: Controller confirms where the carriage is right now.
  controller  ! InquireWithCarriage(1)

  // Give all Actors enough time to finish their work before ...
  Thread.sleep(3000)

  // .. shutting the system down.
  system.terminate

}
