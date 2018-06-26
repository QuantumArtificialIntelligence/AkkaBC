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
    val stringBuffer = new StringBuffer
    var i = 0
    while ( {
      i < arrayBytes.length
    }) {
      stringBuffer.append(Integer.toString((arrayBytes(i) & 0xff) + 0x100, 16).substring(1))

      {
        i += 1; i - 1
      }
    }
    stringBuffer.toString
  }
}
