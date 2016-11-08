package org.nirmalya.entities

import akka.actor.{Actor, ActorLogging, ActorRef, FSM}
import org.nirmalya.common.entities.{PassengerRequestsATransportTo, _}

/**
  * Created by nirmalya on 31/10/16.
  */
class InlaidButtonPanel (val noOfFloors: Int = 10, attachedToCarriage: ActorRef) extends Actor
  with FSM[LiftState,LiftData]
  with ActorLogging{

  //The panel of buttons, each representing a floor
  var possibleDestinations = Vector[Int](noOfFloors)

  startWith(Ready,InitialData)

  when (Ready) {
    case Event(PassengerRequestsATransportTo(floorIDs),_)  =>
         attachedToCarriage ! PassengerRequestsATransportTo(floorIDs)
         stay
  }


}
