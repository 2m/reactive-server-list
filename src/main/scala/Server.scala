package rsl
package actor

import akka.actor.{Props, ActorRef, Actor}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class Server extends Actor {

  import Server._

  def infoActor = context.actorOf(Props(classOf[Queried]))

  val Lobby.ServerActorName(game, ip, port) = self.path.name

  var latestInfo = Message.InfoResponse.empty

  context.system.scheduler.schedule(0.seconds, requestPeriod, self, Message.Update)

  override def receive = {
    case (requester: ActorRef, Message.InfoRequest) => {
      sender ! (requester, latestInfo)
    }
    case Message.Update => infoActor ! Queried.Message.ServerInfoRequest(game, ip, port)
    case Queried.Message.ServerInfoResponse(game, name, map, playerCount) =>
      latestInfo = Message.InfoResponse(game, name, map, playerCount)
  }

}

object Server {
  val requestPeriod = 1.minute

  object Message {
    case object InfoRequest
    case class InfoResponse(game: String, name: String, map: String, playerCount: String)
    object InfoResponse {
      val empty = InfoResponse("N/A", "N/A", "N/A", "N/A")
    }

    private[Server] val Update = "update"
  }
}
