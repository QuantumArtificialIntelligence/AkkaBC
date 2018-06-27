package qai.research.blockchain

import akka.actor.{Actor, ActorLogging}
import qai.research.blockchain.BasicBlock._
import qai.research.blockchain.Blockchain.GetBlockInfo

class BasicBlock(id: String, data: String, lastHash: String) extends Actor with ActorLogging {


  val blockInfo = {
    val bi = BlockInfo(id, Blockchain.getCurrentTimestamp, data, lastHash, "")
    bi.copy(hash = HashGeneratorUtils.generateSHA256(bi.toString))
  }

  def receive = {
    case GetBlockInfo => sender() ! blockInfo
  }
}

object BasicBlock {
  case class BlockInfo(id: String, timestamp: Long, data: String, previousHash: String, hash: String, additionalData: String = "")
}
