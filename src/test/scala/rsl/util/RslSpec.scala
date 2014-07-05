package rsl
package util

import akka.testkit.{TestKit, ImplicitSender}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import akka.cluster.Cluster
import akka.cluster.MemberStatus.Up

class RslSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with
  WordSpecLike with Matchers with BeforeAndAfterAll {

  def this(name: String, config: Config = ConfigFactory.empty()) = this(ActorSystem(
    name,
    config.withFallback(ConfigFactory.load())
  ))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

}

class RslClusterSpec(_system: ActorSystem) extends RslSpec(_system) {

  def this(name: String) = {
    this(ActorSystem(
      name,
      ConfigFactory.parseString(
        """
          |akka.actor.provider = akka.cluster.ClusterActorRefProvider
          |akka.remote.netty.tcp.port = 0
        """.stripMargin)
        .withFallback(ConfigFactory.defaultReference())
    ))
  }

  override def beforeAll = {
    val cluster = Cluster(system)
    val address = cluster.selfAddress
    cluster.join(address)

    // wait until joined to cluster
    awaitCond(cluster.state.members.exists { m =>
      m.address == address && m.status == Up
    })
  }

}
