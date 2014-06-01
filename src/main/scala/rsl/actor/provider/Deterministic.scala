package rsl
package actor
package provider

import akka.actor.Actor
import rsl.actor.provider.Message.{ServerInfoResponse, ServerInfoRequest}
import scala.util.Random

class Deterministic extends Actor {
  override def receive = {
    case ServerInfoRequest(game, ip, port) =>
      sender ! ServerInfoResponse(
        game,
        ip,
        port,
        "Server Name",
        "de_dust",
        "de_dust2",
        ((secs: Long) => {
          s"${(secs / 60).toInt}:${(secs % 60).toInt}"
        })(600 - (System.currentTimeMillis() / 1000) % 600),
        Random.nextInt(16).toString,
        "32"
      )
  }
}
