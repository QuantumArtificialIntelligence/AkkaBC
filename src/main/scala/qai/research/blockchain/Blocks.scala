package qai.research.blockchain

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, ReceiveTimeout}
import qai.research.blockchain.Blocks.NewBlock
import akka.pattern.ask
import qai.research.blockchain.Blockchain.GetLastBlockHash

import scala.concurrent.ExecutionContext.Implicits.global

class Blocks(blockchain: ActorRef) extends Actor with ActorLogging {
  import java.util.concurrent.TimeUnit
  implicit  val timeout: akka.util.Timeout = akka.util.Timeout(2000, TimeUnit.MILLISECONDS)

  override def preStart(): Unit = { }

  def receive = {
    case NewBlock(data) =>
      (blockchain ? GetLastBlockHash).mapTo[String].map( lastHash => {
        val nb = Blockchain.AddBlock(context.system.actorOf(Props(new BasicBlock("random", data, lastHash))))
        Blockchain.currentBlockchain.get ! nb
      })
  }

}

object Blocks {

  case class NewBlock(data: String)

  var currentBlocks: Option[ActorRef] = None

  def main(args: Array[String]): Unit = {


    val nas = Blockchain.actorSystem.get.actorOf(Props(new Blocks(Blockchain.currentBlockchain.get)), name = "blocks")

    currentBlocks = Some(nas)

  }
}
