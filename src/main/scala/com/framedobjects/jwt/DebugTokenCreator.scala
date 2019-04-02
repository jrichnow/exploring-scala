package com.framedobjects.jwt

import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}

object DebugTokenCreator {

  def main(args: Array[String]): Unit = {
    val header = JwtHeader("HS256")
    val claimsSet = JwtClaimsSet(Map(
      "user-name" -> "Jens",
      "trace-id"-> "jens-testing-ibb-migration",
      "iss" -> "stroeer-ssp",
      "ssp-version" -> "1.0.2",
      "override-ip" -> "93.127.158.201",
      "partner_ids" -> ""))

    val jwt = JsonWebToken(header, claimsSet, "LQ$!dRg9Swd^BM4nVNl28EnI^g^NwJZm")
    println(jwt)
  }
}