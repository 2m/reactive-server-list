package rsl.actor

import akka.actor.{ActorRef, Props, Actor}
import rsl.actor.Scheduler.{ReSchedule, Job}
import scala.concurrent.duration._

class Scheduler(jobs: Seq[Job]) extends Actor {

  var scheduledJobs = jobs.map(schedule).toMap

  def schedule(job: Job) = {
    import context.dispatcher
    job.once match {
      case true => job -> context.system.scheduler.scheduleOnce(job.period, job.target, job.msg)
      case false => job -> context.system.scheduler.schedule(0.seconds, job.period, job.target, job.msg)
    }
  }

  def receive = {
    case ReSchedule(job) => {
      scheduledJobs(job).cancel()
      scheduledJobs -= job
      scheduledJobs += schedule(job)
    }
  }

  override def postStop = {
    scheduledJobs.foreach {
      case (_, task) => task.cancel()
    }
  }
}

object Scheduler {

  case class Job(target: ActorRef, msg: Any, period: FiniteDuration, once: Boolean)

  case class ReSchedule(job: Job)

  def props(jobs: Seq[Job]) = {
    Props(classOf[Scheduler], jobs)
  }

}
