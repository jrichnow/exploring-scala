package com.framedobjects.jwt

import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}

object DebugTokenCreator {

  def main(args: Array[String]): Unit = {
    val header = JwtHeader("HS256")
    val claimsSet = JwtClaimsSet(Map(
      "user-name" -> "Jens",
      "trace-id"-> "jens-testing-ppv",
      "iss" -> "stroeer-ssp",
      "ssp-version" -> "1.0.2",
      "partner_ids" -> "1"))

    val jwt = JsonWebToken(header, claimsSet, "LQ$!dRg9Swd^BM4nVNl28EnI^g^NwJZm")
    println(jwt)
  }
}