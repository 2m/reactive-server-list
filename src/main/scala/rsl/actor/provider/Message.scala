package rsl
package actor
package provider

object Message {
  case class ServerInfoRequest(game: String, ip: String, port: String)
  case class ServerInfoResponse(game: String, name: String, map: String, playerCount: String)
}
