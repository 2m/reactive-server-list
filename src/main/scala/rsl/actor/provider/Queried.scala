package rsl
package actor
package provider

import akka.actor.Actor
import scala.concurrent.Future
import net.sourceforge.queried.QueriEd
import scala.concurrent.ExecutionContext.Implicits.global
import rsl.actor.provider.Message.{ServerInfoRequest, ServerInfoResponse}

class Queried extends Actor {
  override def receive = {
    case ServerInfoRequest(game, ip, port) =>
      Future(QueriEd.serverQuery(game.toUpperCase, ip, port.toInt)).onSuccess { case info =>
        sender ! ServerInfoResponse(
          info.getGame,
          ip,
          port,
          info.getName,
          info.getMap,
          "N/A",
          info.getTimeLimit,
          info.getPlayerCount,
          info.getMaxPlayers
        )
      }
  }
}
