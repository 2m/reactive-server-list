package rsl.model

import akka.actor.{ReceiveTimeout, Actor, ActorLogging, Props}
import com.typesafe.config.Config
import rsl.model.DatabaseProvider.Provide

import scala.concurrent.duration._
import scala.slick.jdbc.meta.MTable
import scala.slick.lifted.TableQuery

class DatabaseProvider extends Actor {

  context.setReceiveTimeout(10.seconds)

  def receive = {
    case msg @ Provide(config) => {
      val providerClass = config.getString("provider") match {
        case "h2" =>
          import rsl.model.DatabaseProvider.H2
          classOf[H2]
      }

      val provider = context.actorOf(Props(providerClass))
      provider forward msg
    }
    case ReceiveTimeout => context.stop(self)
  }
}

object DatabaseProvider {

  import scala.slick.backend.DatabaseComponent

  case class Provide(config: Config)
  case class Db(db: DatabaseComponent#DatabaseDef)

  class H2 extends Actor with ActorLogging {

    val servers = TableQuery[Servers]

    import scala.slick.driver.H2Driver.backend.Session
    import scala.slick.driver.H2Driver.simple._

    def receive = {
      case msg @ Provide(config) => {
        log.info("Creating H2 database.")
        val db = Database.forURL(config.getString("url"), driver = config.getString("driver"))
        implicit val session = db.createSession()
        createSchema
        insertData
        sender() ! Db(db)
      }
    }

    def createSchema(implicit session: Session) = {
      if (MTable.getTables.list.find(_.name.name.equalsIgnoreCase("servers")).isEmpty) {
        servers.ddl.create
      }
    }

    def insertData(implicit session: Session) = {
      if (servers.list.isEmpty) {
        servers +=("localhost", 27015)
        servers +=("localhost", 27016)
      }
    }
  }
}
