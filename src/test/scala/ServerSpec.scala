package rsl
package actor

import akka.testkit.{TestActorRef, TestProbe, ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike, Matchers}
import akka.actor.{Props, ActorSystem}
import scala.concurrent.duration._

class ServerSpec extends TestKit(ActorSystem("ServerSpec")) with ImplicitSender with
  WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  val (game, ip, port) = (Lobby.HalfLife, "127.0.0.1", "27015")

  val infoProbe = TestProbe()
  val server = TestActorRef(new Server { override val infoActor = infoProbe.ref }, Lobby.ServerActorName(game, ip, port))

  val deadSender = system.deadLetters

  "A server actor" should {
    "get game, ip and port from name" in {
      server.underlyingActor.game shouldBe "hl"
      server.underlyingActor.ip shouldBe "127.0.0.1"
      server.underlyingActor.port shouldBe "27015"

      // no infoActor should be created when testing
      server.underlyingActor.context.children.size shouldBe 0
    }

    "answer with server info" in {
      within(1.second) {
        server ! (deadSender, Server.Message.InfoRequest)
        expectMsg((deadSender, Server.Message.InfoResponse.empty))
      }
    }

    "should query info actor for server info" in {
      within(Server.requestPeriod) {
        infoProbe.expectMsg(Queried.Message.ServerInfoRequest(game.id, ip, port))
      }
    }
  }

}
