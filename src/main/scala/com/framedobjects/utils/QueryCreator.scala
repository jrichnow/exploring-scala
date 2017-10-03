package com.framedobjects.utils

object QueryCreator {

  def main(args: Array[String]) {
    val errorCodes = Seq(100,101,102, 200, 201, 202, 203, 300, 301, 302, 303, 400, 401, 402, 403, 405, 500, 501, 502, 503, 600, 601, 602, 603, 604, 900, 901)
    val innerSelects = errorCodes.map(mapSelect(_, errorCodes))

    val outerSelectErrorFields = errorCodes.map(code => s"sum(e$code) as '$code'").mkString(", ")

    val outerSelectPrefix = s"select day, $outerSelectErrorFields from ("
    val outerSelectPostfix = s") as f group by day order by day;"
    val entireSelect = s"$outerSelectPrefix\n${innerSelects.mkString("\nunion\n")}\n$outerSelectPostfix"

    println(entireSelect)
  }

  private def mapSelect(errorCode: Int, errorCodes: Seq[Int]): String = {
    val errorSelects = errorCodes.map(mapSingleSelect(_, errorCode)).mkString(", ")
    s"(select date_trunc('day', date) as day, $errorSelects from counters_rtb where type_id = 9$errorCode group by day, type_id order by day) "
  }

  private def mapSingleSelect(errorCode: Int, checkErrorCode: Int): String = {
    if (errorCode == checkErrorCode) s"count(*) as 'e$errorCode'"
    else s"0 as 'e$errorCode'"
  }
}
