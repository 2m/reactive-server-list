package rsl
package actor

import rsl.util.RslClusterSpec
import akka.actor.Props
import rsl.actor.Server.Message.InfoResponse

class StreamerSpec extends RslClusterSpec("StreamerSpec") {

  val streamer = system.actorOf(Props(classOf[Streamer]))

  "Streamer" must {
    "publish to receivers" in {
      streamer ! Streamer.Message.RegisterForAnyServer(testActor)
      streamer ! InfoResponse.empty
      expectMsg(InfoResponse.empty)

      streamer ! Streamer.Message.RegisterForAnyServer(testActor)
      streamer ! InfoResponse.empty
      expectMsg(InfoResponse.empty)

      expectNoMsg()
    }
  }

}
