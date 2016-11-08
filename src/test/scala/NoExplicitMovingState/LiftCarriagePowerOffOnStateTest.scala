package NoExplicitMovingState

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestFSMRef, TestKit}
import common.StopSystemAfterAll
import org.nirmalya.carriages.types.noExplicitMovingState.LiftCarriageImmediate
import org.nirmalya.common.entities.{InstructedToPowerOff, InstructedToPowerOn, PoweredOff, PoweredOn}
import org.nirmalya.entities._
import org.scalatest.{MustMatchers, WordSpecLike}

/**
  * Created by nirmalya on 26/10/16.
  */

class LiftCarriagePowerOffOnStateTest extends TestKit(ActorSystem("testsystem"))
  with WordSpecLike
  with MustMatchers
  with ImplicitSender
  with StopSystemAfterAll {

   val fsm = TestFSMRef(new LiftCarriageImmediate)

  override def beforeAll(): Unit = {
    super.beforeAll()
  }

  "A LiftCarriage" must {
    "Be in PoweredOff state when created" in {
      assert(fsm.stateName  == PoweredOff)
    }

    "should stay in same state, if it receives PowerYourselfOff event when PoweredOff" in {
      fsm ! InstructedToPowerOff
      assert (fsm.stateName == PoweredOff)
    }

    "should be powered on, if it receives PowerYourselfOn event when PoweredOff" in {
      fsm ! InstructedToPowerOn
      assert (fsm.stateName == PoweredOn)
    }
  }
}
