package rsl.actor

import akka.actor.{Props, Actor}
import rsl.model.{ServerInfo, ServerTopic, GameServer}
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator.Publish
import rsl.actor.ClusterServer.Message.{DwellingAt, StartDwelling}
import akka.serialization.Serialization

class ClusterServer(serverProps: Option[Props]) extends Actor {

  val mediator = DistributedPubSubExtension(context.system).mediator

  override def receive = {
    case StartDwelling(gameServer) =>
      context.actorOf(serverProps.getOrElse(Props(classOf[Server], self, None)), Server.ActorName(gameServer))
      sender ! DwellingAt(Serialization.serializedActorPath(self))
    case m: ServerInfo =>
      mediator ! Publish(ServerTopic.id, m)
  }
}

object ClusterServer {
  object Message {
    case class StartDwelling(gameServer: GameServer)
    case class DwellingAt(address: String)
  }
}
