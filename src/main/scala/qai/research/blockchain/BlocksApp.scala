package qai.research.blockchain

import qai.research.blockchain.Blockchain.PrintBlockchain

object BlocksApp {
  def main(args: Array[String]): Unit = {
    Blockchain.main(Array("2551"))
  }

  def printBlockchain(): Unit = {
    Blockchain.currentBlockchain.foreach(_ ! PrintBlockchain)
  }

  def newBlock(data: String): Unit = {
    Blocks.currentBlocks.foreach(cb => {
      (1 to 1000).foreach { i =>
        cb ! Blocks.NewBlock(i + " " + data)
      }
    })
  }
}