package qai.research.blockchain

import java.security.MessageDigest

import scala.util.{Failure, Success, Try}


object HashGeneratorUtils {

  def generateSHA256(message: String): String = hashString(message, "SHA-256")

  private def hashString(message: String, algorithm: String) = {
    Try {
      val digest = MessageDigest.getInstance(algorithm)
      val hashedBytes = digest.digest(message.getBytes("UTF-8"))
      convertByteArrayToHexString(hashedBytes)
    } match {
      case Success(str) => str
      case Failure(exc) => throw new Exception("Oops please check this out: " + exc.getMessage)
    }
  }

  private def convertByteArrayToHexString(arrayBytes: Array[Byte]) = {
    arrayBytes.map(byte => {
      Integer.toString((byte & 0xff) + 0x100, 16).substring(1)
    }).mkString
  }
}
