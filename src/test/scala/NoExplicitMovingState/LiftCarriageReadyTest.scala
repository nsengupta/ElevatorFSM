package NoExplicitMovingState

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestFSMRef, TestKit}
import common.StopSystemAfterAll
import org.nirmalya.carriages.types.noExplicitMovingState.LiftCarriageImmediate
import org.nirmalya.common.entities._
import org.nirmalya.entities._
import org.scalatest.{MustMatchers, WordSpecLike}

/**
  * Created by nirmalya on 26/10/16.
  */

class LiftCarriageReadyTest extends TestKit(ActorSystem("testsystem"))
  with WordSpecLike
  with MustMatchers
  with ImplicitSender
  with StopSystemAfterAll {

   val fsm = TestFSMRef(new LiftCarriageImmediate)

  override def beforeAll(): Unit = {
    super.beforeAll()
  }

  "A LiftCarriage" must {
    "be ready, when it settles down after being PoweredOn" in {
      fsm ! InstructedToPowerOn
      fsm ! BeReady
      assert(fsm.stateName == Ready)
      expectMsg(StoppedAt(0))
    }

    "move to where a passenger is waiting, if it has stopped" in {
      fsm ! InstructedToPowerOn
      fsm ! BeReady
      assert(fsm.stateName == Ready)
      expectMsg(StoppedAt(0))
      fsm ! PassengerIsWaitingAt(3)
      assert(fsm.stateName == Stopped)
      expectMsg(StoppedAt(3))
    }

    "let a passenger in and transport her to her destination" in {
      fsm ! InstructedToPowerOn
      fsm ! BeReady
      assert(fsm.stateName == Ready)
      expectMsg(StoppedAt(0))
      fsm ! PassengerIsWaitingAt(3)
      assert(fsm.stateName == Stopped)
      expectMsg(StoppedAt(3))
      fsm ! PassengerRequestsATransportTo(Vector(6))
      expectMsg(TransportingPassengerTo(6))
      assert(fsm.stateName == Stopped)
      fsm ! ReportCurrentFloor
      expectMsg(6)
    }
  }
}
