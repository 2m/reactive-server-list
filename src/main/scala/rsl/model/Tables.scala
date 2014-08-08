package rsl.model

import scala.slick.driver.H2Driver.simple._

class Servers(tag: Tag)
  extends Table[(String, Int)](tag, "SERVERS") {

  def address = column[String]("ADDRESS")
  def port: Column[Int] = column[Int]("PORT")

  def *  = (address, port)
}
