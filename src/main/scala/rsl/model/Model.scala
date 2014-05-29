package rsl
package model

case class GameServer(game: Game, address: String, port: String)

sealed trait Topic {
  val id: String
}
case object ServerTopic extends Topic {
  val id = "server"
}

sealed trait Game {
  val id: String
}
case object HalfLife extends Game { val id = "hl" }
