package rsl.actor

import rsl.util.RslSpec
import akka.actor.Props
import rsl.actor.Scheduler.{ReSchedule, Job}
import scala.concurrent.duration._

class SchedulerSpec extends RslSpec("SchedulerSpec") {

  "scheduler" must {
    "schedule jobs from constructor" in {
      system.actorOf(Props(classOf[Scheduler], Seq(Job(self, "msg", 100.millis, once = true))))
      within(200.millis) {
        expectMsg("msg")
        expectNoMsg()
      }
    }
    "reschedule jobs" in {
      val job = Job(self, "msg", 200.millis, once = true)
      val sch = system.actorOf(Props(classOf[Scheduler], Seq(job)))
      within(100.millis) {
        expectNoMsg()
      }

      sch ! ReSchedule(job)

      within(150.millis) {
        expectNoMsg()
      }
      within(100.millis) {
        expectMsg("msg")
      }
    }
  }

}
