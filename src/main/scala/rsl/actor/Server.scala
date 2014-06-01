package rsl.actor

import akka.actor.{Cancellable, Props, ActorRef, Actor}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import rsl.model.{ServerInfo, Game, GameServer}
import java.util.concurrent.TimeUnit

class Server(val infoSink: ActorRef, infoProviderProps: Option[Props]) extends Actor {

  import Server._

  val infoProvider = context.actorOf(infoProviderProps.getOrElse {
    context.system.settings.config.getString("rsl.provider") match {
      case "queried" => Props(classOf[provider.Queried])
      case "deterministic" | _ => Props(classOf[provider.Deterministic])
    }
  })
  val ActorName(game, ip, port) = self.path.name

  var latestInfo = ServerInfo.empty
  var updateTicker: Option[Cancellable] = None

  val requestPeriod = context.system.settings.config.getDuration("rsl.request-period", TimeUnit.SECONDS).seconds

  override def preStart = {
    updateTicker = Some(context.system.scheduler.schedule(0.seconds, requestPeriod, self, Message.Update))
  }

  override def receive = {
    case (requester: ActorRef, Message.InfoRequest) => {
      sender ! (requester, latestInfo)
    }
    case Message.Update =>
      infoProvider ! provider.Message.ServerInfoRequest(game, ip, port)
    case provider.Message.ServerInfoResponse(game, address, port, name, map, nextMap, timeLeft, playerCount, playerMax) =>
      latestInfo = ServerInfo(game, address, port, name, map, nextMap, timeLeft, playerCount, playerMax)
      infoSink ! latestInfo
  }

  override def postStop = {
    updateTicker.foreach(_.cancel())
  }

}

object Server {
  object Message {
    case object InfoRequest

    private[Server] val Update = "update"
  }

  object ActorName {
    def apply(gs: GameServer): String = apply(gs.game, gs.address, gs.port)
    def apply(game: Game, ip: String, port: String) = s"${game.id}-$ip-$port"
    def unapply(name: String) = name.split("-").toList match {
      case game :: ip :: port :: Nil => Some((game, ip, port))
      case _ => None
    }
  }
}
