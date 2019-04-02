package com.framedobjects.um

import java.io.{BufferedWriter, FileWriter}
import java.lang.{Long => JLong}
import java.sql.{Connection, DriverManager}

import com.framedobjects.sql.DbUtil
import javax.management.{Attribute, AttributeList, MBeanServerConnection, ObjectName}
import javax.management.remote.{JMXConnectorFactory, JMXServiceURL}

import scala.collection.mutable.ArrayBuffer

object JmxUserMatchStats {

  val csvOutFile = "/users/jensr/Temp/user-match-attempts.csv"

  def main(args: Array[String]): Unit = {
    val partners = dspPartners(true)
    val ihs = Seq("app01")
    var overallStats = ArrayBuffer[UserMatchAttemptStats]()  // TODO convert to an mutable map
    for (ih <- ihs) {
      val handlerStats = getStatsFromOneHandler(partners,ih, 11083)
      if (overallStats.isEmpty) {
        overallStats = handlerStats
      }
      else {
        // TODO combine overall and handler stats
      }
      generateCsvOutput(handlerStats)
    }

    println("overallStats")
    writeCsv(overallStats)

//    val stats = getStatsFromOneHandler(partners,"app01", 11083)
//    val stats2 = getStatsFromOneHandler(partners,"app02", 11083

//    generateGnuPlotData(stats)
  }

  private def getStatsFromOneHandler(partners: Seq[Partner], app:String, jmxPort:Int): ArrayBuffer[UserMatchAttemptStats] = {
    println(s"getting stats from handler: $app")
    val connection: MBeanServerConnection = jmxConnection(app, jmxPort)
    var stats = ArrayBuffer[UserMatchAttemptStats]()
    for (partner <- partners) {
      println(s"getting stats for ${partner.name} (${partner.id})")
      stats += getStats(partner, connection)
    }
    stats
  }

  private def generateCsvOutput(stats: ArrayBuffer[UserMatchAttemptStats]): Unit = {
    println("DSP Name,DSP ID,Attempts,Success,Standdown,1st attempt,2nd attempt,3rd attempt," +
      "PartnerInitiatedUserMatchCount,PartnerInitiatedVastUserMatchAttempt, PartnerInitiatedVastUserMatchCount," +
      "aver UserMatchTime,min UserMatchTime,max UserMatchTime")
    for (stat <- stats) {
      println(stat.toCsv)
    }
  }

  private def generateGnuPlotData(stats: ArrayBuffer[UserMatchAttemptStats]): Unit = {
    println(s"#partner, ${stats.map(_.partner.name).mkString(", ")}")
    println(s"1, ${stats.map(_.rtbStats.adscaleInitiatedUserMatchAttempts).mkString(", ")}")
    println(s"2, ${stats.map(_.rtbStats.adscaleInitiatedUserMatchCount).mkString(", ")}")
    println(s"3, ${stats.map(_.standdownCount).mkString(", ")}")
    println(s"4, ${stats.map(_.oneAttempt).mkString(", ")}")
    println(s"5, ${stats.map(_.twoAttempts).mkString(", ")}")
    println(s"6, ${stats.map(_.threeAttempts).mkString(", ")}")
    println(s"7, ${stats.map(_.rtbStats.averageUserMatchingTime).mkString(", ")}")
    println(s"8, ${stats.map(_.rtbStats.minUserMatchingTime).mkString(", ")}")
    println(s"9, ${stats.map(_.rtbStats.maxUserMatchingTime).mkString(", ")}")
  }

  private def writeCsv(lineItems: ArrayBuffer[UserMatchAttemptStats]): Unit = {
    val bw = new BufferedWriter(new FileWriter(csvOutFile))
    val header = Seq("DSP Name","DSP ID","Attempts","Success","Standdown","1st attempt","2nd attempt","3rd attempt","PartnerInitiatedUserMatchCount","PartnerInitiatedVastUserMatchAttempt", "PartnerInitiatedVastUserMatchCount","aver UserMatchTime","min UserMatchTime","max UserMatchTime").mkString(",")
    bw.write(header.toString + "\n")
    lineItems.foreach(l => bw.write(l.toCsv + "\n"))
    bw.close()
  }

  private def getStats(partner: Partner, connection: MBeanServerConnection): UserMatchAttemptStats = {
    val userMatchMetrics = new ObjectName("Adscale:domain=adserver,app=ih,subsystem=tpui,name=UserMatchingMetrics")
    val one = Option(getAttemptCount(userMatchMetrics, partner.id, 1, connection)).getOrElse("0").toLong
    val two = Option(getAttemptCount(userMatchMetrics, partner.id, 2, connection)).getOrElse("0").toLong
    val three = Option(getAttemptCount(userMatchMetrics, partner.id, 3, connection)).getOrElse("0").toLong
    val standdownCount = Option(getStanddownCount(userMatchMetrics, partner.id, connection)).getOrElse("0").toLong

    val partnerKey = s"${partner.name.toLowerCase()}-${partner.id}"
    val adscaleUserMatchMetrics = new ObjectName(s"Adscale:domain=adserver,app=ih,subsystem=rtb,partner=$partnerKey,name=rtbStats")
    val rtbStats = getRtbStatsAttributes(adscaleUserMatchMetrics, connection)
    println(rtbStats)

    UserMatchAttemptStats(partner, standdownCount, one, two, three, rtbStats)
  }

  private def getAttemptCount(objectName: ObjectName, partnerId: Int, attempt: Int, connection: MBeanServerConnection): String = {
    val result = connection.invoke(objectName, "getAttemptCount", Array(new JLong(partnerId), new Integer(attempt)), Array("long", "int"))
    Option(result).getOrElse("0").toString
  }

  private def getStanddownCount(objectName: ObjectName, partnerId: Int, connection: MBeanServerConnection): String = {
    val result = connection.invoke(objectName, "getStanddownCount", Array(new JLong(partnerId)), Array("long"))
    Option(result).getOrElse("0").toString
  }

  private def getRtbStatsAttributes(objectName: ObjectName, connection: MBeanServerConnection): RtbStats = {
    val attributeList = connection.getAttributes(objectName, Array(
      "AdscaleInitiatedUserMatchAttempt",
      "AdscaleInitiatedUserMatchCount",
      "PartnerInitiatedUserMatchCount",
      "PartnerInitiatedVastUserMatchAttempt",
      "PartnerInitiatedVastUserMatchCount",
      "VastUserMatchAttempt", // DH
      "VastUserMatchCount",
      "AverageUserMatchingTime",
      "MinUserMatchingTime",
      "MaxUserMatchingTime",
      "RequestsWithUserMatchCount",
      "RequestsWithoutUserMatchCount"))
//    println(attributeList)
    val adscaleInitiatedUserMatchAttempts = getValueForKey(attributeList, "AdscaleInitiatedUserMatchAttempt")
    val adscaleInitiatedUserMatchCounts = getValueForKey(attributeList, "AdscaleInitiatedUserMatchCount")
    val partnerInitiatedUserMatchCount = getValueForKey(attributeList, "PartnerInitiatedUserMatchCount")
    val partnerInitiatedVastUserMatchAttempt = getValueForKey(attributeList, "PartnerInitiatedVastUserMatchAttempt")
    val partnerInitiatedVastUserMatchCount = getValueForKey(attributeList, "PartnerInitiatedVastUserMatchCount")
    val vastUserMatchAttempt = getValueForKey(attributeList, "VastUserMatchAttempt")
    val vastUserMatchCount = getValueForKey(attributeList, "VastUserMatchCount")
    val averageUserMatchingTime = getValueForKey(attributeList, "AverageUserMatchingTime")
    val minUserMatchingTime = getValueForKey(attributeList, "MinUserMatchingTime")
    val maxUserMatchingTime = getValueForKey(attributeList, "MaxUserMatchingTime")
    val requestsWithUserMatch = getValueForKey(attributeList, "RequestsWithUserMatchCount")
    val requestsWithoutUserMatch = getValueForKey(attributeList, "RequestsWithoutUserMatchCount")
    RtbStats(adscaleInitiatedUserMatchAttempts,
      adscaleInitiatedUserMatchCounts,
      partnerInitiatedUserMatchCount,
      partnerInitiatedVastUserMatchAttempt,
      partnerInitiatedVastUserMatchCount,
      vastUserMatchAttempt,
      vastUserMatchCount,
      averageUserMatchingTime,
      minUserMatchingTime,
      maxUserMatchingTime,
      requestsWithUserMatch,
      requestsWithoutUserMatch)
  }

  private def getValueForKey(attributeList: AttributeList, attributeName: String): Long = {
    val attributes = attributeList.asList()
    var value: Long = 0
    for (attribute <- attributes.toArray()) {
      val a = attribute.asInstanceOf[Attribute]
      value = a.getName match {
        case `attributeName` => a.getValue.asInstanceOf[Long]
        case _ => value
      }
    }
    value
  }

  private def jmxConnection(app:String, jmxPort:Int): MBeanServerConnection = {
    val serviceUrl = s"service:jmx:rmi:///jndi/rmi://$app:$jmxPort/jmxrmi"
    val serviceURL = new JMXServiceURL(serviceUrl)
    val connector = JMXConnectorFactory.connect(serviceURL)
    connector.getMBeanServerConnection()
  }

  private def dspPartners(fromDb: Boolean): Seq[Partner] = {
    if (fromDb) {
      getDspPartnersFromDb
    }
    else {
      getFixedListOfDspPartners
    }
  }

  private def getDspPartnersFromDb: Seq[Partner] = {
    println("Getting partners from DB.")
    implicit val connection: Connection = DbUtil.getPostgresDbConnection

    val statement = connection.createStatement()
    val resultSet = statement.executeQuery(
      "select base.id, partner_name " +
        "from rtb_partner_base base join rtb_dsp on base.id=rtb_dsp.id join rtb_user_matching um on base.id=um.id " +
        "where enabled is true and bid_request_enabled is true and callee is true order by partner_name")
    var partners = ArrayBuffer[Partner]()
    while (resultSet.next()){
      partners += Partner(resultSet.getString(2), resultSet.getInt(1))
    }
    partners
  }

  private def getFixedListOfDspPartners: Seq[Partner] = {
    Seq(
      Partner("ActiveAgent", 72),
      Partner("Adform", 42),
      Partner("AdPilot_OpenRtb", 84),
      Partner("AppNexus_OpenRtb", 75),
      Partner("Bidswitch", 57),
      Partner("Criteo", 40),
      Partner("DynAdmic", 74),
      Partner("IndexExchange", 63),
      Partner("Invite2", 38),
      Partner("MBR", 48),
      Partner("MediaMath", 39),
      Partner("RIVITY", 23),
      Partner("SmartStream", 68),
      Partner("TheTradeDesk", 60),
      Partner("Twiago", 77),
      Partner("Twiago_Native", 62),
      Partner("Zemanta", 35)
    )
  }
}

case class Partner(name: String, id: Int)

case class UserMatchAttemptStats(partner: Partner,
                                 standdownCount: Long,
                                 oneAttempt: Long,
                                 twoAttempts: Long,
                                 threeAttempts: Long,
                                 rtbStats: RtbStats) {

  def toCsv: String = {
    s"${partner.name},${partner.id},${rtbStats.adscaleInitiatedUserMatchAttempts}," +
      s"${rtbStats.adscaleInitiatedUserMatchCount},$standdownCount,$oneAttempt,$twoAttempts,$threeAttempts," +
      s"${rtbStats.partnerInitiatedUserMatchCount},${rtbStats.partnerInitiatedVastUserMatchAttempt}," +
      s"${rtbStats.partnerInitiatedVastUserMatchCount},${rtbStats.averageUserMatchingTime}," +
      s"${rtbStats.minUserMatchingTime},${rtbStats.maxUserMatchingTime}"
  }
}

case class RtbStats(adscaleInitiatedUserMatchAttempts: Long,
                    adscaleInitiatedUserMatchCount: Long,
                    partnerInitiatedUserMatchCount: Long,
                    partnerInitiatedVastUserMatchAttempt: Long,
                    partnerInitiatedVastUserMatchCount: Long,
                    vastUserMatchAttempt: Long,
                    vastUserMatchCount: Long,
                    averageUserMatchingTime: Long,
                    minUserMatchingTime: Long,
                    maxUserMatchingTime: Long,
                    requestsWithUserMatch: Long,
                    requestsWithoutUserMatch: Long)

