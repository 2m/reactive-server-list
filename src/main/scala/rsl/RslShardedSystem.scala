package rsl

import akka.actor.{ActorRef, Props, ActorSystem}
import rsl.model.GameServer
import akka.contrib.pattern.{ShardRegion, ClusterSharding}
import rsl.actor.{Streamer, Server, ClusterServer}
import rsl.actor.ClusterServer.Message.{DwellingAt, StartDwelling}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import akka.cluster.Cluster
import scala.concurrent.Promise
import com.typesafe.config.{ConfigFactory, Config}

class RslShardedSystem(config: Config) {
  val system = ActorSystem("rsl", config.withFallback(ConfigFactory.load()))
  val shardCount = 10
  implicit val timeout = Timeout(2.seconds)

  var streamer: Option[ActorRef] = None

  val idExtractor: ShardRegion.IdExtractor = {
    case m @ StartDwelling(gameServer) => (Server.ActorName(gameServer), m)
  }

  val shardResolver: ShardRegion.ShardResolver = idExtractor andThen {
    case (entryId: String, _) => (entryId.hashCode() % shardCount).toString
  }

  def init = {
    val cluster = Cluster(system)
    val clusterSharding = ClusterSharding(system)

    clusterSharding.start(
      typeName = "server",
      entryProps = Some(Props(classOf[ClusterServer], None)),
      idExtractor = idExtractor,
      shardResolver = shardResolver
    )

    streamer = Some(system.actorOf(Props[Streamer]))

    val startPromise = Promise[RslShardedSystem]()
    cluster.registerOnMemberUp {
      startPromise.success(this)
    }
    startPromise.future
  }

  def addServer(gameServer: GameServer) = {
    val serverShard = ClusterSharding(system).shardRegion("server")

    (serverShard ? StartDwelling(gameServer)).mapTo[DwellingAt]
  }

  def addListener(listener: ActorRef) = {
    streamer.foreach(_ ! Streamer.Message.RegisterForAnyServer(listener))
  }

  def shutdown = {
    system.shutdown()
  }
}

object RslShardedSystem {
  def apply(config: Config = ConfigFactory.empty()) = new RslShardedSystem(config).init
}
