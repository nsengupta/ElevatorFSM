package withExplicitMovingState


import akka.actor.{ActorSystem, Props}
import akka.actor.FSM.{CurrentState, SubscribeTransitionCallBack, Transition}
import akka.testkit.{ImplicitSender, TestFSMRef, TestKit, TestProbe}

import scala.concurrent.duration._
import common.StopSystemAfterAll
import org.nirmalya.carriages.types.withExplicitMovingState.LiftCarriageWithMovingState
import org.nirmalya.common.entities._
import org.nirmalya.common.entities.DefaultMovingStateSimulatorActor
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpecLike}

/**
  * Created by nirmalya on 26/10/16.
  */

class LiftCarriageMovingStoppingCorrectlyTest extends TestKit(ActorSystem("Lift-system"))
  with WordSpecLike
  with MustMatchers
  with BeforeAndAfterAll
  with ImplicitSender
  with StopSystemAfterAll {

  val movingStateSimulator = system.actorOf(Props(DefaultMovingStateSimulatorActor))

  override def beforeAll(): Unit = {
    super.beforeAll()
  }

  "A LiftCarriage" must {
    "be ready, when it settles down after being PoweredOn" in {

      val testCarriageFSM = system.actorOf(
        LiftCarriageWithMovingState.props(movingStateSimulator),"Carriage1"
      )
      val testProbe = TestProbe()

      testCarriageFSM ! SubscribeTransitionCallBack(testProbe.ref)

      testProbe.expectMsgPF() {
        case CurrentState(_,PoweredOff) => true
      }

      testCarriageFSM ! InstructedToPowerOn

      testProbe.expectMsgPF() {
        case Transition(_,PoweredOff,PoweredOn) => true
      }
    }

    "move to where a passenger is waiting, if ready" in {

      val testCarriageFSM = system.actorOf(
        LiftCarriageWithMovingState.props(movingStateSimulator),"Carriage2"
      )

      val testProbe = TestProbe()
      testCarriageFSM ! SubscribeTransitionCallBack(testProbe.ref)

      testProbe.expectMsgPF() {
        case CurrentState(_,PoweredOff) => true
      }

      testCarriageFSM ! InstructedToPowerOn
      testProbe.expectMsgPF() {
        case Transition(_,PoweredOff,PoweredOn) => true
      }
      testCarriageFSM ! BeReady

      testProbe.expectMsgPF() {
        case Transition(_,PoweredOn,Ready) => true
      }

      testCarriageFSM ! PassengerIsWaitingAt(3)

      testProbe.expectMsgPF() {
        case Transition(_,Ready,Moving) => true
      }

      testProbe.expectMsgPF() {
        case Transition(_,Moving,Stopped) => true
      }

      testCarriageFSM ! ReportCurrentFloor
      expectMsg(StoppedAt("Carriage2",3))

      // After waiting for 1000 ms (hardcoded, I am embarrassed) in Stopped state,
      // the FSM times out the state, and moves to Ready. So, 2 seconds is a good
      // length of time for us to expect that to happen.
      within (2 seconds) {
        testProbe.expectMsgPF() {
          case Transition(_,Stopped,Ready) => true
        }
      }
    }
    "let a passenger in and transport her to her destination" in {

      val testCarriageFSM = system.actorOf(
        LiftCarriageWithMovingState.props(movingStateSimulator),"Carriage3"
      )

      testCarriageFSM ! InstructedToPowerOn
      testCarriageFSM ! BeReady
      testCarriageFSM ! PassengerIsWaitingAt(3)
      testCarriageFSM ! PassengerRequestsATransportTo(Vector(7))

      testCarriageFSM ! ReportCurrentFloor
      expectMsgPF (1 seconds) {
        case StoppedAt("Carriage3",7)  => true
        case StoppedAt("Carriage3",0)  => true
        case _                         => false
      }

    }

    "let three passengers in and transport them to their destinations" in {

      val testCarriageFSM = system.actorOf(
        LiftCarriageWithMovingState.props(movingStateSimulator),"Carriage4"
      )

      val testProbe = TestProbe()
      testCarriageFSM ! SubscribeTransitionCallBack(testProbe.ref)


      testProbe.expectMsgPF() {
        case CurrentState(_,PoweredOff) => true
      }

      testCarriageFSM ! InstructedToPowerOn
      testProbe.expectMsgPF() {
        case Transition(_,PoweredOff,PoweredOn) => true
      }
      testCarriageFSM ! BeReady

      testProbe.expectMsgPF() {
        case Transition(_,PoweredOn,Ready) => true
      }

      testCarriageFSM ! PassengerRequestsATransportTo(Vector(2,6,8,2,2))

      testProbe.expectMsgAllOf(
        10 seconds,
        Transition  (testCarriageFSM,Ready,Moving),
        Transition  (testCarriageFSM,Moving,Stopped),
        Transition  (testCarriageFSM,Stopped,Moving),
        Transition  (testCarriageFSM,Moving,Stopped)
      )

      testCarriageFSM ! ReportCurrentFloor
      expectMsg(StoppedAt("Carriage4",6))

      testProbe.expectMsgAllOf(// Carriage comes to Ready when no more pending floors exist.
        10 seconds,
        Transition  (testCarriageFSM,Stopped,Moving),
        Transition  (testCarriageFSM,Moving,Stopped),
        Transition  (testCarriageFSM,Stopped,Ready)

      )
      testCarriageFSM ! ReportCurrentFloor
      expectMsg(StoppedAt("Carriage4",8))
    }
  }

  "should visit same floor only once till it comes back to Ready" in {

    val testCarriageFSM = system.actorOf(
      LiftCarriageWithMovingState.props(movingStateSimulator),"Carriage5"
    )

    val testProbe = TestProbe()
    testCarriageFSM ! SubscribeTransitionCallBack(testProbe.ref)


    testProbe.expectMsgPF() {
      case CurrentState(_,PoweredOff) => true
    }

    testCarriageFSM ! InstructedToPowerOn
    testProbe.expectMsgPF() {
      case Transition(_,PoweredOff,PoweredOn) => true
    }
    testCarriageFSM ! BeReady

    testProbe.expectMsgPF() {
      case Transition(_,PoweredOn,Ready) => true
    }

    testCarriageFSM ! PassengerRequestsATransportTo(Vector(2,6,2,2))

    testProbe.expectMsgAllOf(
      10 seconds,
      Transition  (testCarriageFSM,Ready,Moving),
      Transition  (testCarriageFSM,Moving,Stopped)
    )

    testCarriageFSM ! ReportCurrentFloor
    expectMsg(StoppedAt("Carriage5",2))

    testProbe.expectMsgAllOf( // Carriage comes to Ready when no more pending floors exist.
      10 seconds,
      Transition  (testCarriageFSM,Stopped,Moving),
      Transition  (testCarriageFSM,Moving,Stopped),
      Transition  (testCarriageFSM,Stopped,Ready)
    )

    testCarriageFSM ! ReportCurrentFloor
    expectMsg(StoppedAt("Carriage5",6))

  }

}
