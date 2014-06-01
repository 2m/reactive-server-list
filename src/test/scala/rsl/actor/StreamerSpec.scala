package rsl
package actor

import rsl.util.RslClusterSpec
import akka.actor.Props
import rsl.model.ServerInfo

class StreamerSpec extends RslClusterSpec("StreamerSpec") {

  val streamer = system.actorOf(Props(classOf[Streamer]))

  "Streamer" must {
    "publish to receivers" in {
      streamer ! Streamer.Message.RegisterForAnyServer(testActor)
      streamer ! ServerInfo.empty
      expectMsg(ServerInfo.empty)

      streamer ! Streamer.Message.RegisterForAnyServer(testActor)
      streamer ! ServerInfo.empty
      expectMsg(ServerInfo.empty)

      expectNoMsg()
    }
  }

}
