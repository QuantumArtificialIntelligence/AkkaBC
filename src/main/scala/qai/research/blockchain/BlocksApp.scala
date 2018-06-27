package qai.research.blockchain

import akka.actor.{ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import qai.research.blockchain.Blockchain.PrintBlockchain


// A test Object to start the Blockchain
object BlocksApp extends App {
  // Override the configuration of the port when specified as program argument
  val port = if (args.isEmpty) "0" else args(0)

  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
    withFallback(ConfigFactory.parseString("akka.cluster.roles = [blockchain]")).
    withFallback(ConfigFactory.load("akkablockchain"))

  val system = ActorSystem("ClusterSystem", config)

  val blockchain = system.actorOf(Props(new Blockchain("ActorsSuperBlockchain")), name = "blockchainActor")

  val blocks = system.actorOf(Props(new Blocks(blockchain)))

  system.actorOf(Props[MetricsListener], name = "metricsListener")

  def printBlockchain(): Unit = {
    blockchain ! PrintBlockchain
  }

  def newBlock(data: String): Unit = {
    blocks ! Blocks.NewBlock(data)
  }
}