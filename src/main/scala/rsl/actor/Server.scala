package rsl.actor

import akka.actor.{Props, ActorRef, Actor}
import scala.concurrent.duration._
import rsl.model.{ServerInfo, Game, GameServer}
import java.util.concurrent.TimeUnit
import rsl.actor.Scheduler.{ReSchedule, Job}

class Server(val infoSink: ActorRef, infoProviderProps: Option[Props], schedulerProps: Option[Props]) extends Actor {

  import Server._

  val infoProvider = context.actorOf(infoProviderProps.getOrElse {
    context.system.settings.config.getString("rsl.provider") match {
      case "queried" => Props(classOf[provider.Queried])
      case "deterministic" | _ => Props(classOf[provider.Deterministic])
    }
  })

  val requestPeriod = context.system.settings.config.getDuration("rsl.request-period", TimeUnit.SECONDS).seconds
  val stopPeriod = context.system.settings.config.getDuration("rsl.shutdown-period", TimeUnit.SECONDS).seconds

  val updateJob = Job(self, Message.Update, requestPeriod, once = false)
  val stopJob = Job(self, Message.Stop, stopPeriod, once = true)

  val scheduler = context.actorOf(schedulerProps.getOrElse(Scheduler.props(Seq(updateJob, stopJob))))

  val ActorName(game, ip, port) = self.path.name

  var latestInfo = ServerInfo.empty

  override def receive = {
    case (requester: ActorRef, Message.InfoRequest) => {
      sender ! (requester, latestInfo)
    }
    case Message.Update =>
      infoProvider ! provider.Message.ServerInfoRequest(game, ip, port)
    case Message.Stop =>
      context.stop(self)
    case provider.Message.ServerInfoResponse(game, address, port, name, map, nextMap, timeLeft, playerCount, playerMax) =>
      latestInfo = ServerInfo(game, address, port, name, map, nextMap, timeLeft, playerCount, playerMax)
      infoSink ! latestInfo

      scheduler ! ReSchedule(stopJob)
  }

}

object Server {
  object Message {
    case object InfoRequest

    private[Server] val Update = "update"
    private[Server] val Stop = "stop"
  }

  object ActorName {
    def apply(gs: GameServer): String = apply(gs.game, gs.address, gs.port)
    def apply(game: Game, ip: String, port: String) = s"${game.id}-$ip-$port"
    def unapply(name: String) = name.split("-").toList match {
      case game :: ip :: port :: Nil => Some((game, ip, port))
      case _ => None
    }
  }

  def props(infoSink: ActorRef) = Props(classOf[Server], infoSink, None, None)
}
