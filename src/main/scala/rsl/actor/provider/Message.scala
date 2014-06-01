package rsl
package actor
package provider

object Message {
  case class ServerInfoRequest(game: String, ip: String, port: String)
  case class ServerInfoResponse(
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
}
