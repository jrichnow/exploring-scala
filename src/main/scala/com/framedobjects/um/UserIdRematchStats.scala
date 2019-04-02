package com.framedobjects.um

import java.io.{BufferedWriter, FileWriter}
import java.lang.{Long => JLong}

import com.framedobjects.sql.DbUtil
import javax.management.remote.{JMXConnectorFactory, JMXServiceURL}
import javax.management.{MBeanServerConnection, ObjectName}
import java.sql.Connection


object UserIdRematchStats {

  type statsByPartner = (DspUserConfig, String, String)
  val csvOutFile = "/users/jensr/Temp/user-match-stickiness.csv"

  def main(args: Array[String]): Unit = {
    implicit val dbConn = DbUtil.getPostgresDbConnection
    implicit val jmxConn = jmxConnection

    val result = getPartnerIds
    println(s"partnerIds: $result")
    val dspUserConfigs = getDspUserMatchConfig(result)
    dspUserConfigs.foreach(println)

    val partnerIdsStats = dspUserConfigs.map(mapStats(_))
    partnerIdsStats.foreach(println)

    val partnerIdStatsInclPerc = partnerIdsStats.map(x => DspUserMatchStickiness(x._1, x._2, x._3, (x._3.toFloat / x._2.toFloat) * 100))
    writeCsv(partnerIdStatsInclPerc)
  }

  private def getDspUserMatchConfig(partnerIds: String)(implicit dbConn: Connection): Seq[DspUserConfig] ={
    val statement = dbConn.createStatement()
    val rs = statement.executeQuery(s"select b.id, partner_name, callee from rtb_partner_base b join rtb_user_matching u on b.id=u.id where b.id in ($partnerIds)")
    var dspUserConfigs = Seq[DspUserConfig]()
    while (rs.next()) {
      val dspUserConfig = DspUserConfig(rs.getInt(1),rs.getString(2), rs.getBoolean(3))
      dspUserConfigs = dspUserConfigs :+ dspUserConfig
    }
    dspUserConfigs
  }

  private def mapStats(dspUserConfig: DspUserConfig)(implicit connection: MBeanServerConnection): statsByPartner = {
    (dspUserConfig, getUpdateAttempts(dspUserConfig.id.toString), getUpdatedPartnerIds(dspUserConfig.id.toString))
  }

  private def getUpdateAttempts(partnerId: String)(implicit connection: MBeanServerConnection): String = {
    val objectName = new ObjectName("Adscale:domain=adserver,app=ih,subsystem=userTracking,name=ThirdPartyUserIdMetrics")
    val result = connection.invoke(objectName, "getThirdPartyUserIdUpdateAttempts", Array(new JLong(partnerId)), Array("long"))
    println(s"getUpdateCount for $partnerId")
    Option(result).getOrElse("0").toString
  }

  private def getUpdatedPartnerIds(partnerId: String)(implicit connection: MBeanServerConnection): String = {
    val objectName = new ObjectName("Adscale:domain=adserver,app=ih,subsystem=userTracking,name=ThirdPartyUserIdMetrics")
    val result = connection.invoke(objectName, "getThirdPartyUserIdUpdatedByPartnerId", Array(new JLong(partnerId)), Array("long"))
    println(s"getUpdatedIds for $partnerId")
    Option(result).getOrElse("0").toString
  }

  private def getPartnerIds(implicit connection: MBeanServerConnection): String = {
    val objectName = new ObjectName("Adscale:domain=adserver,app=ih,subsystem=userTracking,name=ThirdPartyUserIdMetrics")
    val result = connection.invoke(objectName, "getExistingPartnerIds", Array(), Array())
    Option(result).getOrElse("").toString
  }

  private def jmxConnection(): MBeanServerConnection = {
    val serviceUrl = "service:jmx:rmi:///jndi/rmi://app01:11083/jmxrmi"
    val serviceURL = new JMXServiceURL(serviceUrl)
    val connector = JMXConnectorFactory.connect(serviceURL)
    connector.getMBeanServerConnection()
  }

  private def writeCsv(lineItems: Seq[DspUserMatchStickiness]): Unit = {
    val bw = new BufferedWriter(new FileWriter(csvOutFile))

    val header = Seq("partnerId", "partnerName", "callee", "userRematchRequests", "changedUserId", "ratio").mkString(",")
    bw.write(header.toString + "\n")

    lineItems.sortBy(_.dspUserConfig.id).foreach(l => bw.write(l.toCsv + "\n"))
    bw.close()
  }

  case class DspUserConfig(id:Int, name:String, callee:Boolean)

  case class DspUserMatchStickiness(dspUserConfig: DspUserConfig, attempts:String, updated:String, ratio:Float) {
    def toCsv(): String = {
      s"${dspUserConfig.id},${dspUserConfig.name},${dspUserConfig.callee},$attempts,$updated,$ratio"
    }
  }

}
