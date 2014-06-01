package rsl.actor

import akka.testkit.TestProbe
import akka.actor._
import rsl.model.{ServerInfo, HalfLife, ServerTopic, GameServer}
import rsl.util.TestUtil.Implicits._
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator.{SubscribeAck, Subscribe}
import rsl.util.RslClusterSpec
import rsl.actor.ClusterServer.Message.DwellingAt

class ClusterServerSpec extends RslClusterSpec("ClusterServerSpec") {

  val gameServer = GameServer(HalfLife, "127.0.0.1", "27015")

  val severProbe = TestProbe()

  val clusterServer = system.actorOf(Props(classOf[ClusterServer], Some(severProbe.props)))

  "ClusterServer" must {
    "forward received message from server to topic" in {
      val msg = ServerInfo.empty
      val mediator = DistributedPubSubExtension(system).mediator

      mediator ! Subscribe(ServerTopic.id, testActor)
      expectMsgClass(classOf[SubscribeAck])

      clusterServer ! ClusterServer.Message.StartDwelling(gameServer)
      expectMsgClass(classOf[DwellingAt])

      clusterServer ! msg
      expectMsg(msg)
    }
  }

}
