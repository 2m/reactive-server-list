package rsl
package util

import akka.actor.{Props, Actor, IndirectActorProducer, ActorRef}
import akka.testkit.TestProbe

object TestUtil {

  class ProbeForwarderProducer(val probe: ActorRef) extends IndirectActorProducer {
    override def produce(): Actor = new Actor {
      override def receive = {
        case msg => probe forward msg
      }
    }

    override def actorClass: Class[_ <: Actor] = classOf[Actor]
  }

  object Implicits {
    implicit class RichTestProbe(probe: TestProbe) {
      def props = Props(classOf[ProbeForwarderProducer], probe.ref)
    }
  }
}
