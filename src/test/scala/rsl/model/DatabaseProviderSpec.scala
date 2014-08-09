package rsl
package model

import akka.actor.Props
import com.typesafe.config.ConfigFactory
import rsl.model.DatabaseProvider.{Db, Provide}
import rsl.util.RslSpec

import scala.util.Random

class DatabaseProviderSpec extends RslSpec("DatabaseProviderSpec") {

  "database provider" should {
    "provide h2 database" in {
      val config = ConfigFactory.parseString(s"""
          provider = h2
          url = "jdbc:h2:mem:"
          driver = org.h2.Driver
      """)

      val provider = system.actorOf(Props(classOf[DatabaseProvider]))
      provider ! Provide(config)

      expectMsgPF() {
        case Db(db) =>
      }
    }
  }

}
