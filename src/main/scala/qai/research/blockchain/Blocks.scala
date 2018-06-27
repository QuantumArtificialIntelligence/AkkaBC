package qai.research.blockchain

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import qai.research.blockchain.Blocks.NewBlock
import akka.pattern.ask
import qai.research.blockchain.Blockchain.GetLastBlockHash

import scala.concurrent.ExecutionContext.Implicits.global

class Blocks(blockchain: ActorRef) extends Actor with ActorLogging {
  import java.util.concurrent.TimeUnit
  implicit  val timeout: akka.util.Timeout = akka.util.Timeout(2000, TimeUnit.MILLISECONDS)

  def receive = {
    case NewBlock(data) =>
      (blockchain ? GetLastBlockHash).mapTo[String].map( lastHash => {
        val nb = Blockchain.AddBlock(context.system.actorOf(Props(new BasicBlock("random", data, lastHash))), lastHash)
        blockchain ! nb
      })
  }
}

object Blocks {
  case class NewBlock(data: String)
}
