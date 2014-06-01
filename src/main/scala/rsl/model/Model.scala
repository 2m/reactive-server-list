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

case class ServerInfo(
  game: String,
  address: String,
  port: String,
  name: String,
  map: String,
  nextMap: String,
  timeLeft: String,
  playerCount: String,
  playerMax: String
)
object ServerInfo {
  val empty = ServerInfo("N/A", "N/A", "N/A", "N/A", "N/A", "N/A", "N/A", "N/A", "N/A")
}
