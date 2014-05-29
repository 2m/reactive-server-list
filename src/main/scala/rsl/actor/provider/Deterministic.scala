package rsl
package actor
package provider

import akka.actor.Actor
import rsl.actor.provider.Message.{ServerInfoResponse, ServerInfoRequest}

class Deterministic extends Actor {
  override def receive = {
    case ServerInfoRequest(game, ip, port) =>
      sender ! ServerInfoResponse(
        game,
        "Deterministic Name",
        "Deterministic Map",
        "Deterministic Player Count"
      )
  }
}
