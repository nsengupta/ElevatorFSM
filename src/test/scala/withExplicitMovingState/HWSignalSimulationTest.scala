package withExplicitMovingState

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import common.StopSystemAfterAll
import org.nirmalya.common.entities.{InformMeOnReaching, NextStop, PurposeOfMovement, ReachedFloor}
import org.nirmalya.entities.TimeConsumingHWSignalSimulator
import org.scalatest.time.{Millis, Milliseconds}
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpecLike, time}

import scala.concurrent.duration._

/**
  * Created by nirmalya on 14/11/16.
  */
class HWSignalSimulationTest extends TestKit(ActorSystem("HW-Simulation-system"))
  with WordSpecLike
  with MustMatchers
  with BeforeAndAfterAll
  with ImplicitSender
  with StopSystemAfterAll{

  val hwSignalSimulatorProps = TimeConsumingHWSignalSimulator.props(1)
  val hwSignalSimulator = system.actorOf(hwSignalSimulatorProps)

  "A HWSignalSimulator" must {
    "respond after correctly computed time to travel, has elapsed" in {
      val fromFloorID = 2
      val toNextStop = NextStop(4,PurposeOfMovement.ToAllowATransportedPassengerAlight)

      hwSignalSimulator ! InformMeOnReaching(fromFloorID,toNextStop)

      expectNoMsg(FiniteDuration(1500, TimeUnit.MILLISECONDS))

      within(1 seconds) {
        expectMsg(ReachedFloor(toNextStop))
      }
    }

    }

  "respond immediately if the source and destination floors are the same" in {
    val fromFloorID = 2
    val toNextStop = NextStop(2,PurposeOfMovement.ToAllowATransportedPassengerAlight)

    hwSignalSimulator ! InformMeOnReaching(fromFloorID,toNextStop)

    within(1 seconds) {
      expectMsg(ReachedFloor(toNextStop))
    }
  }

}
