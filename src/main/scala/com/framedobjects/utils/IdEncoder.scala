package com.framedobjects.utils

import java.lang.Long
import sun.misc.BASE64Encoder
import sun.misc.BASE64Decoder
import scala.util.Try

object IdEncoder {

  private val usage = "use either '-e number' to encrypt or '-d encryptedNumber' to decrypt."
  private val rotateValue = 6

  def main(args: Array[String]) {
    args match {
      case a if (a.length < 2 || a.length > 2) => println(usage)
      case b if b(0) == "-d" => println(decrypt(b(1)).getOrElse("Error decrypting"))
      case c if c(0) == "-e" => println(encrypt(c(1).toLong))
      case _ => println(usage)
    }
  }

  private def encrypt(n: Long): String = {
    val shiftedValue = Long.rotateLeft(n, rotateValue)
    new BASE64Encoder().encode(Long.toHexString(shiftedValue).getBytes)
  }

  private def decrypt(n: String):Try[Long] = {
    Try {
      val decodedBytes = new BASE64Decoder().decodeBuffer(n)
      Long.rotateRight(Long.parseLong(new String(decodedBytes), 16), rotateValue)
    }
  }
}