package rsl
package model

import org.scalatest.{BeforeAndAfter, WordSpec}
import rsl.util.RslDatabase

import scala.slick.driver.H2Driver.simple._
import scala.slick.jdbc.meta._

class TablesSpec extends WordSpec with BeforeAndAfter with RslDatabase {

  "correctly configured db" should {
    "create the Schema" in {
      val tables = MTable.getTables.list

      assert(tables.size == 1)
      assert(tables.count(_.name.name.equalsIgnoreCase("servers")) == 1)
    }

    "query servers" in {
      val results = servers.list
      assert(results.size == 2)
      assert(results.head._1 == "localhost")
    }
  }
}