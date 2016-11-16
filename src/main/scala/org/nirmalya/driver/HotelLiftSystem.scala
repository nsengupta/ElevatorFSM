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
    val carriage               = system.actorOf(LiftCarriageWithMovingState.props(hwSignalSimulator), s"Carriage_$i")
    val buttonPanel            = system.actorOf(InlaidButtonPanel.props(i,carriage),s"ButtonPanel_$i")
    Lift(carriage,buttonPanel,hwSignalSimulator)
  }

  val controller = system.actorOf(LiftController.props(lifts.map(l => l.carriage).toVector),"Controller")

  controller ! InstructedToPowerOn

  // Thread.sleep(1000)

  controller ! BeReady

  controller ! PassengerIsWaitingAt(2)

  controller ! PassengerIsWaitingAt(4)

  controller  ! InquireWithCarriage(1)

  Thread.sleep(3000)

  lifts(0).buttonPanel ! PassengerRequestsATransportTo(Vector(5))

  lifts(1).buttonPanel ! PassengerRequestsATransportTo(Vector(6,2))

  Thread.sleep(3000)

  controller  ! InquireWithCarriage(0)

  controller  ! InquireWithCarriage(1)

  Thread.sleep(3000)

  controller  ! InquireWithCarriage(0)

  controller  ! InquireWithCarriage(1)

  Thread.sleep(3000)

  controller  ! InquireWithCarriage(0)

  controller  ! InquireWithCarriage(1)

  Thread.sleep(2000)

  controller  ! InquireWithCarriage(1)

  Thread.sleep(3000)

  system.terminate

}
