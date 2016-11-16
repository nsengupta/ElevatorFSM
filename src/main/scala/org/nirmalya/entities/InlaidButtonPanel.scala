package org.nirmalya.entities

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import org.nirmalya.common.entities.{PassengerRequestsATransportTo, _}

/**
  * Created by nirmalya on 31/10/16.
  */
class InlaidButtonPanel (panelID: Int, attachedToCarriage: ActorRef, val noOfFloors: Int = 10) extends Actor
  with ActorLogging{

  //The panel of buttons, each representing a floor
  // TODO [NS]: max and min floorIDs must be checked.
  var possibleDestinations = Vector[Int](noOfFloors)

  override def receive: Receive = {
    case (PassengerRequestsATransportTo(floorIDs))  =>
      attachedToCarriage ! PassengerRequestsATransportTo(floorIDs)
      log.debug(
        s"InlaidButtonPanel($panelID): ${attachedToCarriage.path.name}, needs to drop passengers at floors(${floorIDs.mkString(",")}).")
    case (x: Any) =>
      log.debug(s"InlaidButtonPanel ($panelID): received unknown message ($x).")
  }
}

object InlaidButtonPanel {
  def props(panelID: Int, belongsToCarriage: ActorRef) = Props(new InlaidButtonPanel(panelID,belongsToCarriage))
}
