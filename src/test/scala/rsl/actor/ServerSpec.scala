package rsl.actor

import akka.testkit.{TestActorRef, TestProbe}
import akka.actor.{Terminated, Props}
import scala.concurrent.duration._
import rsl.util.RslSpec
import rsl.util.TestUtil.Implicits._
import rsl.model.{ServerInfo, HalfLife}
import akka.dispatch.sysmsg.DeathWatchNotification

class ServerSpec extends RslSpec("ServerSpec") {

  val (game, ip, port) = (HalfLife, "127.0.0.1", "27015")

  val infoSink = TestProbe()
  val infoProvider = TestProbe()
  val server = TestActorRef(Props(classOf[Server], infoSink.ref, Some(infoProvider.props), None), Server.ActorName(game, ip, port))

  val deadSender = system.deadLetters

  "A server actor" should {
    "get game, ip and port from name" in {
      server.underlyingActor.asInstanceOf[Server].game shouldBe "hl"
      server.underlyingActor.asInstanceOf[Server].ip shouldBe "127.0.0.1"
      server.underlyingActor.asInstanceOf[Server].port shouldBe "27015"
    }

    "answer with server info" in {
      within(1.second) {
        server ! (deadSender, Server.Message.InfoRequest)
        expectMsg((deadSender, ServerInfo.empty))
      }
    }

    "should query info actor for server info" in {
      within(server.underlyingActor.asInstanceOf[Server].requestPeriod) {
        infoProvider.expectMsg(provider.Message.ServerInfoRequest(game.id, ip, port))
      }
    }

    "should send info to sink when received from provider" in {
      server ! provider.Message.ServerInfoResponse("game", "address", "ip", "name", "map", "nextMap", "timeLeft", "playerCount", "playerMax")
      infoSink.expectMsg(ServerInfo("game", "address", "ip", "name", "map", "nextMap", "timeLeft", "playerCount", "playerMax"))
    }

    "should stop when receiving no messages from provider" in {
      watch(server)
      within(server.underlyingActor.asInstanceOf[Server].stopPeriod * 2) { // some leeway
        expectMsgClass(classOf[Terminated])
      }
    }
  }

}
