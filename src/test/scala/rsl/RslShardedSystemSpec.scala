package rsl

import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import rsl.model.{ServerInfo, HalfLife, GameServer}
import scala.concurrent.Await
import scala.concurrent.duration._
import rsl.util.RslSpec
import com.typesafe.config.ConfigFactory
import scala.util.Random

class RslShardedSystemSpec extends RslSpec("RslShardedSystemSpec")
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  import RslShardedSystemSpec._

  implicit val timeout = 2.seconds

  var rslOption: Option[RslShardedSystem] = None

  "RSL sharded system" must {
    "be initialized" in {
      rslOption = Some(Await.result(RslShardedSystem(rslConfig), timeout))
    }

    "accept new servers" in {
      val Some(rsl) = rslOption
      val server1 = rsl.addServer(GameServer(HalfLife, "127.0.0.1", "27015"))
      val server1DwellingAt = Await.result(server1, timeout)
      println(s"Server 1 is dwelling at: $server1DwellingAt")

      val server2 = rsl.addServer(GameServer(HalfLife, "127.0.0.1", "27016"))
      val server2DwellingAt = Await.result(server2, timeout)
      println(s"Server 2 is dwelling at: $server2DwellingAt")
    }

    "receive server updates" in {
      val Some(rsl) = rslOption

      rsl.addListener(testActor)
      receiveWhile() {
        case m: ServerInfo => println(m)
      }
    }
  }

  override def afterAll {
    super.afterAll()

    rslOption.foreach(_.shutdown)
  }

}

object RslShardedSystemSpec {
  val rslConfig = {
    val port = Random.nextInt(50000) + 5000
    ConfigFactory.parseString(
      s"""
      |rsl.request-period = 1 second
      |akka {
      | remote.netty.tcp.port = $port
      | cluster.seed-nodes = ["akka.tcp://rsl@127.0.0.1:$port"]
      |}
    """.stripMargin)
  }
}
