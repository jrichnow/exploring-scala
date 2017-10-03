package com.framedobjects.um

import java.io.{File, PrintWriter}

import scala.io.Source

object UserMatchCsv {

  type PartnerToUserStatsMap = Map[String, UserMatchStats]
  type PartnerToDayToUserMatchStats = Map[String, Map[String, UserMatchStats]]

  val directory = "/users/jensr/Documents/DevNotes/investigations/um/spark-dsp/"

  def main(args: Array[String]): Unit = {
    val files = getListOfSubDirectories(directory).filter(_.endsWith("csv")).toSeq

    var combinedMap: PartnerToDayToUserMatchStats = Map[String, Map[String, UserMatchStats]]().empty
    for (file <- files) {
      val userMatchStats = getUserMatchStatsForFile(s"$directory$file")
      val partnerToUserStatsMap = toMap(userMatchStats)
      combinedMap = addToDayMap(file.split("\\.")(0), partnerToUserStatsMap, combinedMap)
    }

    writeCsv(combinedMap, files, true)
  }

  def writeCsv(combinedMap: PartnerToDayToUserMatchStats, files: Seq[String], videoOnly: Boolean): Unit = {
    val dayHeader = files.map(f => {
      val day = f.split("\\.")(0)
      s"$day-um,$day-num,$day-perc"
    }).mkString(",")
    var csv = s"partner,$dayHeader\n"

    var seqOfTuples = combinedMap.toSeq
    if (videoOnly) {
      seqOfTuples = combinedMap.toSeq.filter(_._1.contains("display"))
    }

    seqOfTuples.sortBy(_._1).foreach { x =>
      var csvLine = ""
      csvLine += x._1
      for (file <- files) {
        val day = file.split("\\.")(0)
        csvLine += ","
        csvLine += x._2.getOrElse(day, UserMatchStats("", "", 0L, 0L, 0.0)).toCsv()
      }
      csvLine += "\n"
      csv += csvLine
    }
    println(csv)

    val pw = new PrintWriter(new File(s"$directory/output/combined-display.csv"))
    pw.write(csv)
    pw.close
  }


  def toMap(userMatchStats: Seq[UserMatchStats]): PartnerToUserStatsMap = {
    userMatchStats.map(s => (s"${s.partnerId}-${s.kind}", s)).toMap
  }

  def addToDayMap(day: String, partnerMap: PartnerToUserStatsMap, combinedMap: PartnerToDayToUserMatchStats): PartnerToDayToUserMatchStats = {
    var thisMap = combinedMap
    for (partnerId <- partnerMap.keys) {
      if (combinedMap.contains(partnerId)) {
        thisMap = addDayToCombinedMap(day, partnerId, partnerMap(partnerId), thisMap)
      }
      else {
        thisMap = addPartnerToCombinedMap(day, partnerId, partnerMap(partnerId), thisMap)
      }
    }

    def addDayToCombinedMap(day: String, partnerId: String, userMatchStats: UserMatchStats, combinedMap: PartnerToDayToUserMatchStats): PartnerToDayToUserMatchStats = {
      val partnerToDaysMap = combinedMap(partnerId)
      val daysMap = partnerToDaysMap + (day -> userMatchStats)
      combinedMap + (partnerId -> daysMap)
    }

    def addPartnerToCombinedMap(day: String, partnerId: String, userMatchStats: UserMatchStats, combinedMap: PartnerToDayToUserMatchStats): PartnerToDayToUserMatchStats = {
      combinedMap + (partnerId -> Map(day -> userMatchStats))
    }

    thisMap
  }

  private def getUserMatchStatsForFile(file: String): Seq[UserMatchStats] = {
    val csv = Source.fromFile(file).getLines().toSeq.tail
    csv.map(toStats)
  }

  private def getListOfSubDirectories(directoryName: String): Array[String] = {
    new File(directoryName).listFiles.map(_.getName)
  }

  private def toStats(line: String): UserMatchStats = {
    val array = line.split(",")
    UserMatchStats(array(0), array(1), array(2).toLong, array(3).toLong, array(4).toDouble)
  }
}

case class UserMatchStats(partnerId: String, kind: String, userMatchCount: Long, noUserMatchCount: Long, percentage: Double) {

  def toCsv(): String = {
    s"$userMatchCount,$noUserMatchCount,$percentage"
  }

}
