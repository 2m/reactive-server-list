package rsl
package actor

import akka.actor.{ActorRef, Props, Actor}
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

class Lobby extends Actor {

  import Lobby._

  def serverActor: Actor = new Server

  override def receive = {
    case Message.InfoRequest(game, ip, port) => {
      val serverActorName = ServerActorName(game, ip, port)
      val originalSender = sender
      context.actorSelection(serverActorName).resolveOne(resolveTimeout).onComplete {
        case Success(server) => self ! Message.ResolvedServer(originalSender, server)
        case Failure(_) => {
          self ! Message.ResolvedServer(originalSender, context.actorOf(Props(serverActor), serverActorName))
        }
      }
    }
    case Message.ResolvedServer(requester, server) => {
      server ! (requester, Server.Message.InfoRequest)
    }
    case (requester: ActorRef, response: Server.Message.InfoResponse) => requester ! response
    case Message.CountServers => sender ! context.children.size
  }
}

object Lobby {
  val resolveTimeout = 5.seconds

  object ServerActorName {
    def apply(game: Game, ip: String, port: String) = s"${game.id}-$ip-$port"
    def unapply(name: String) = name.split("-").toList match {
      case game :: ip :: port :: Nil => Some((game, ip, port))
      case _ => None
    }
  }

  object Message {
    case class InfoRequest(game: Game, ip: String, port: String)
    case object CountServers

    private[Lobby] case class ResolvedServer(requester: ActorRef, server: ActorRef)
  }

  sealed trait Game {
    val id: String
  }
  case object HalfLife extends Game { val id = "hl" }
}
