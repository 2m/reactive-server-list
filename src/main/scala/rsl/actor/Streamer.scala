package rsl
package actor

import akka.actor.{Terminated, ActorRef, Actor}
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator.Subscribe
import rsl.model.ServerTopic
import rsl.actor.Server.Message.InfoResponse

class Streamer extends Actor {
  import Streamer.Message._

  val mediator = DistributedPubSubExtension(context.system).mediator

  var serverReceivers: Set[ActorRef] = Set.empty

  override def preStart = {
    mediator ! Subscribe(ServerTopic.id, self)
  }

  override def receive = {
    case RegisterForAnyServer(ref) =>
      context.watch(ref)
      serverReceivers += ref
    case Terminated(ref) =>
      context.unwatch(ref)
      serverReceivers -= ref
    case m: InfoResponse =>
      serverReceivers.foreach(_ ! m)
  }
}
object Streamer {
  object Message {
    case class RegisterForAnyServer(ref: ActorRef)
  }
}
