package rsl
package actor

import akka.actor.Props
import akka.contrib.pattern.{ClusterSharding, ShardRegion}
import akka.testkit.TestProbe
import org.scalatest.BeforeAndAfter
import rsl.RslShardedSystem.Shards
import rsl.actor.Animator.Animate
import rsl.actor.ClusterServer.Message.StartDwelling
import rsl.model.{GameServer, HalfLife}
import rsl.util.TestUtil.Implicits._
import rsl.util.{RslClusterSpec, RslDatabase}

class AnimatorSpec extends RslClusterSpec("AnimatorSpec") with BeforeAndAfter with RslDatabase {

  "animator" should {
    "reanimate all servers" in {

      val idExtractor: ShardRegion.IdExtractor = {
        case m @ _ => ("test-id", m)
      }

      val shardResolver: ShardRegion.ShardResolver = idExtractor andThen {
        case _ => "test-shard"
      }

      val serverShardProbe = TestProbe()
      ClusterSharding(system).start(Shards.server, Some(serverShardProbe.props), idExtractor, shardResolver)

      val reanimator = system.actorOf(Props(classOf[Animator], db))
      reanimator ! Animate

      serverShardProbe.expectMsg(StartDwelling(GameServer(HalfLife, "localhost", "27015")))
      serverShardProbe.expectMsg(StartDwelling(GameServer(HalfLife, "localhost", "27016")))
    }
  }

}
