package rsl
package actor

import akka.testkit.{TestProbe, ImplicitSender, TestKit}
import akka.actor.{Actor, Props, ActorSystem}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._

class LobbySpec extends TestKit(ActorSystem("LobbySpec")) with ImplicitSender with
  WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  val serverProbe = TestProbe()
  val lobby = system.actorOf(Props(new Lobby {
    override def serverActor = new Actor {
      override def receive = {
        case msg => serverProbe.ref forward msg
      }
    }
  }), "lobby")

  def interactWithLobby(ip: String) = {
    lobby ! Lobby.Message.InfoRequest(Lobby.HalfLife, ip, "27015")
    within(1.second) {
      serverProbe.expectMsg((testActor, Server.Message.InfoRequest))

      val expectedMessage = Server.Message.InfoResponse("game", "name", "map", "playerCount")
      serverProbe.send(lobby, (testActor, expectedMessage))
      expectMsg(expectedMessage)
    }
  }

  def expectLobbyServers(expected: Int) = {
    lobby ! Lobby.Message.CountServers
    expectMsg(expected)
  }

  "lobby" should {
    "create an actor for new server" in {
      interactWithLobby("127.0.0.1")
      expectLobbyServers(1)
    }
    "reuse actor for a seen server" in {
      interactWithLobby("127.0.0.1")
      expectLobbyServers(1)
    }
    "create an actor for a second server" in {
      interactWithLobby("127.0.0.2")
      expectLobbyServers(2)
    }
  }

}
