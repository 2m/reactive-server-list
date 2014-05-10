package rsl
package actor

import akka.actor.Actor
import rsl.actor.Queried.Message.{ServerInfoRequest, ServerInfoResponse}
import scala.concurrent.Future
import net.sourceforge.queried.QueriEd
import scala.concurrent.ExecutionContext.Implicits.global

class Queried extends Actor {
  override def receive = {
    case ServerInfoRequest(game, ip, port) =>
      Future(QueriEd.serverQuery(game.toUpperCase, ip, port.toInt)).onSuccess { case info =>
        sender ! ServerInfoResponse(
          info.getGame,
          info.getName,
          info.getMap,
          info.getPlayerCount
        )
      }
  }
}

object Queried {
  object Message {
    case class ServerInfoRequest(game: String, ip: String, port: String)
    case class ServerInfoResponse(game: String, name: String, map: String, playerCount: String)
  }
}
