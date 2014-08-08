package rsl
package actor

import akka.actor.{Actor, ActorLogging}
import akka.contrib.pattern.ClusterSharding
import rsl.RslShardedSystem.Shards
import rsl.actor.ClusterServer.Message.{DwellingAt, StartDwelling}
import rsl.actor.Animator.Animate
import rsl.model.{GameServer, HalfLife, Servers}

import scala.slick.driver.H2Driver.simple._
import scala.slick.jdbc.JdbcBackend.DatabaseDef
import scala.slick.lifted.TableQuery

class Animator(db: DatabaseDef) extends Actor with ActorLogging {

  val servers = TableQuery[Servers]

  def addServer(gameServer: GameServer) = {
    val serverShard = ClusterSharding(context.system).shardRegion(Shards.server)
    serverShard ! StartDwelling(gameServer)
  }

  def receive = {
    case Animate => {
      db.withSession { implicit session =>
        servers.foreach { case (address, port) =>
          addServer(GameServer(HalfLife, address, port.toString))
        }
      }
    }
    case DwellingAt(address, gameServer) => {
      log.info(s"Game server [$gameServer] started at [$address]")
    }
  }
}

object Animator {
  case object Animate
}
