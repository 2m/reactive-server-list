package rsl
package util

import org.scalatest.BeforeAndAfter
import rsl.model.Servers

import scala.slick.driver.H2Driver.simple._
import scala.slick.lifted.TableQuery
import scala.util.Random

trait RslDatabase { self: BeforeAndAfter =>
  val servers = TableQuery[Servers]

  val db = Database.forURL("jdbc:h2:mem:" + new Random().nextString(6), driver = "org.h2.Driver")
  implicit var session: Session = _

  before {
    session = db.createSession()
    servers.ddl.create
    servers += ("localhost", 27015)
    servers += ("localhost", 27016)
  }

  after {
    session.close()
  }
}
