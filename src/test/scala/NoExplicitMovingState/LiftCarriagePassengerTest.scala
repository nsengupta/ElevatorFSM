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

class LiftCarriagePassengerTest extends TestKit(ActorSystem("testsystem"))
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
      fsm ! PassengerRequestsATransportTo(Vector(4,5))
      fail("Not Implemented correctly yet")
      expectMsg(TransportingPassengerTo(4))
      assert(fsm.stateName != Stopped)

    }
  }
}
