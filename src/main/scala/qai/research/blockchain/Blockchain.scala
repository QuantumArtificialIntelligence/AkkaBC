package qai.research.blockchain

import java.util.Date

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import qai.research.blockchain.Blockchain._
import akka.pattern.ask

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

class Blockchain(blockchainId: String) extends Actor with ActorLogging {
  import java.util.concurrent.TimeUnit
  implicit  val timeout: akka.util.Timeout = akka.util.Timeout(2000, TimeUnit.MILLISECONDS)

  val genesis = context.system.actorOf(Props(new BasicBlock("fistBlock", "", "blockchainId")), "fistBlock")

  var blocks = List[BlockRef](genesis)

  var lastBlock = genesis
  var lastHash = getBlockHash(genesis)

  var preProcessStrategy: Option[ActorRef] = None
  var preValidator: Option[ActorRef] = None
  var postValidator: Option[ActorRef] = None

  def receive = {
    case SetPreProcessStrategy(pps) =>
      preProcessStrategy = Some(pps)

    case SetPreValidator(preV) =>
      preValidator = Some(preV)

    case SetPostValidator(postV) =>
      postValidator = Some(postV)

    case addBlock @ AddBlock(block) =>
      preValidator.map(_ ! addBlock).getOrElse(self ! PreValidatedBlock(block))

    case preValidatedBlock @ PreValidatedBlock(block) =>
      preProcessStrategy.map(_ ! preValidatedBlock).getOrElse(self ! ProcessedBlock(block))

    case processedBlock @ ProcessedBlock(block) =>
      postValidator.map(_ ! processedBlock).getOrElse(self ! PostValidatedBlock(block, getBlockInfo(block).lastHash))

    case PostValidatedBlock(block, blockLastHash) if blockLastHash == lastHash =>
      blocks = block :: blocks
      lastBlock = block
      lastHash = getBlockHash(block)

    case PostValidatedBlock(block, blockLastHash) =>
      log.error(s"invalid hash - next version will send to mempool")

    case GetLastBlockHash =>
      sender() ! lastHash

    case PrintBlockchain =>
      blocks.foreach { block =>
        println(s"${getBlockInfo(block).toString}")
      }

  }

}

object Blockchain {
  type BlockRef = ActorRef

  case class SetPreProcessStrategy(stragegy: ActorRef) // This will pre process the block to be able to insert it later
  case class SetPreValidator(preValidator: ActorRef)
  case class SetPostValidator(postValidator: ActorRef)

  case object GetBlockInfo
  case object GetLastBlockHash
  case object PrintBlockchain

  case class AddBlock(block: BlockRef)
  case class PreValidatedBlock(block: BlockRef)
  case class ProcessedBlock(block: BlockRef)
  case class PostValidatedBlock(block: BlockRef, lastHash: String)

  var currentBlockchain: Option[ActorRef] = None
  var actorSystem: Option[ActorSystem] = None

  def main(args: Array[String]): Unit = {

    // Override the configuration of the port when specified as program argument
    val port = if (args.isEmpty) "0" else args(0)

    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [blockchain]")).
      withFallback(ConfigFactory.load("akkablockchain"))

    val system = ActorSystem("ClusterSystem", config)
    actorSystem = Some(system)

    currentBlockchain = Some(system.actorOf(Props(new Blockchain("ActorsSuperBlockchain")), name = "blockchainActor"))

    Blocks.currentBlocks = Some(system.actorOf(Props(new Blocks(Blockchain.currentBlockchain.get))))

    system.actorOf(Props[MetricsListener], name = "metricsListener")
  }

  def getCurrentTimestamp: Long = (new Date).getTime

  def getBlockHash(block: BlockRef): String = {
    getBlockInfo(block).hash
  }

  def getBlockInfo(block: BlockRef): BasicBlock.BlockInfo = {
    import java.util.concurrent.TimeUnit
    Await.result(((block ? GetBlockInfo).mapTo[BasicBlock.BlockInfo]), scala.concurrent.duration.Duration(2000, TimeUnit.MILLISECONDS))
  }
}