package rsl

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.Cluster
import akka.contrib.pattern.{ClusterSharding, ShardRegion}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import rsl.RslShardedSystem.Shards
import rsl.actor.Animator.Animate
import rsl.actor.ClusterServer.Message.StartDwelling
import rsl.actor.{Animator, ClusterServer, Server, Streamer}
import rsl.model.DatabaseProvider
import rsl.model.DatabaseProvider.{Db, Provide}

import scala.concurrent.duration._
import scala.concurrent.Promise

class RslShardedSystem(config: Config) {
  val system = ActorSystem("rsl", config
    .withFallback(ConfigFactory.parseResources("reactive-server-list.conf"))
    .withFallback(ConfigFactory.load()))
  val shardCount = 10
  implicit val timeout = Timeout(2.seconds)

  var streamer: Option[ActorRef] = None
  var animator: Option[ActorRef] = None

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
      typeName = Shards.server,
      entryProps = Some(Props(classOf[ClusterServer], None)),
      idExtractor = idExtractor,
      shardResolver = shardResolver
    )

    streamer = Some(system.actorOf(Props[Streamer]))

    import scala.concurrent.ExecutionContext.Implicits.global
    (system.actorOf(Props[DatabaseProvider]) ? Provide(system.settings.config.getConfig("rsl.db"))).onSuccess {
      case Db(db) => animator = Some(system.actorOf(Props(classOf[Animator], db)))
    }

    val startPromise = Promise[RslShardedSystem]()
    cluster.registerOnMemberUp {
      startPromise.success(this)

      animator.get ! Animate
    }
    startPromise.future
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

  object Shards {
    val server = "server"
  }
}
