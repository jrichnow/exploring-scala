package com.framedobjects.sql

import java.io.{BufferedWriter, FileWriter}
import java.sql.Connection

import scala.io.Source

object SimpleSeedingAllianceInventoryImport {

  val csvInFile = "/users/jensr/Documents/DevNotes/investigations/SA-inventory-import/20180730_seeding_stroeer_digital.csv"
  val csvOutFile = "/users/jensr/Documents/DevNotes/investigations/SA-inventory-import/20180730_seeding_stroeer_digital_updated.csv"
  val seedingAllianceAccountId = 17229

  def main(args: Array[String]): Unit = {
    val lineItems = getLineItems
    println(s"about to create ${lineItems.size} slots")
    lineItems.foreach(println)

    val updatedLineItems = createEntities(lineItems)

    writeCsv(updatedLineItems)
  }

  private def createEntities(lineItems: Seq[ShortLineItem]): Seq[ShortLineItem] = {
    implicit val connection: Connection = DbUtil.getDbConnection
    implicit val doCommit: Boolean = false

    var updatedLineItems = Seq[ShortLineItem]()

    DbUtil.transaction {
      for (lineItem <- lineItems) {
        println(lineItem)
        val websiteOption = getWebsiteByName(lineItem.websiteName)
        val website = websiteOption match {
          case ws: Some[Website] => ws.get
          case None => createWebsite(lineItem)
        }
        println(s"have website: $website")
        val slotId = DbUtil.createSlot(website.id, lineItem.slotName)
        println(s"slot ID: $slotId")

        val updatedLineItem = lineItem.copy(websiteId = Some(website.id), websiteUrl = Some(website.url),
          whitelistedDomains = Some(website.whitelistedDomains), newSlotId = Some(slotId))
        updatedLineItems = updatedLineItems :+ updatedLineItem
      }
    }
    updatedLineItems
  }

  private def getWebsiteUrlAndWhiteListedDomains(oldSlotId: Long)(implicit conn: Connection): (Option[String], Option[String]) = {
    val statement = conn.createStatement()
    val resultSet = statement.executeQuery(s"SELECT url, white_listed_domains FROM website WHERE website_id = (select fk_website_id from slot where id = $oldSlotId)")
    if (resultSet.next()) {
      (Some(resultSet.getString(1)), Some(resultSet.getString(2)))
    }
    else {
      (None, None)
    }
  }

  private def getWebsiteByName(name: String)(implicit conn: Connection): Option[Website] = {
    val statement = conn.createStatement()
    val resultSet = statement.executeQuery(
      s"SELECT website_id, url, white_listed_domains FROM website " +
        s"WHERE name='$name' and fk_account_id=$seedingAllianceAccountId")
    if (resultSet.next()) {
      Some(Website(resultSet.getLong(1), name, resultSet.getString(2), resultSet.getString(3)))
    }
    else {
      None
    }
  }

  private def createWebsite(lineItem: ShortLineItem)(implicit conn: Connection): Website = {
    println(s"creating website for name ${lineItem.websiteName}")
    val (url, whitelistedDomains) = getWebsiteUrlAndWhiteListedDomains(lineItem.oldSlotId)

    val insertWebsite = "insert into website (website_id, name, url, fk_account_id, created_date, mod_time, category, " +
      "white_listed_domains, channel_url) values (nextval('website_seq'), ?, ?, ?, now(), now(), ?, ?, ?)"
    val websiteStatement = conn.prepareStatement(insertWebsite)
    websiteStatement.setString(1, lineItem.websiteName)
    websiteStatement.setString(2, url.getOrElse(""))
    websiteStatement.setLong(3, lineItem.accountID)
    websiteStatement.setString(4, "HOME")
    websiteStatement.setString(5, whitelistedDomains.getOrElse(""))
    websiteStatement.setString(6, url.getOrElse(""))

    websiteStatement.executeUpdate()

    val websiteIdRs = conn.createStatement().executeQuery("select currval('website_seq')")
    websiteIdRs.next()
    val websiteId = websiteIdRs.getLong(1)

    val selectDefaultFilterId = "SELECT id FROM publisher_auto_approval_filter WHERE account_id = ? and is_default is true"
    val filterIdStatement = conn.prepareStatement(selectDefaultFilterId)
    filterIdStatement.setLong(1, lineItem.accountID)
    val filterIdRs = filterIdStatement.executeQuery()
    while (filterIdRs.next()) {
      val filterId = filterIdRs.getLong(1)
      println(s"creating publisher_filter_to_website for website ID $websiteId and filterId $filterId")

      val insertFilter = "INSERT INTO publisher_filter_to_website (website_id, filter_id) VALUES (?, ?)"
      val filterStatement = conn.prepareStatement(insertFilter)
      filterStatement.setLong(1, websiteId)
      filterStatement.setLong(2, filterId)

      filterStatement.executeUpdate()
    }

    Website(websiteId, lineItem.websiteName, url.getOrElse(""), whitelistedDomains.getOrElse(""))
  }

  private def writeCsv(lineItems: Seq[ShortLineItem]): Unit = {
    val bw = new BufferedWriter(new FileWriter(csvOutFile))

    val header = Seq("OldSlotId", "WebsiteName", "WebsiteId", "websiteUrl", "whitelistedDomains", "slotName", "NewSlotId").mkString(",")
    bw.write(header.toString + "\n")

    lineItems.sortBy(_.lineItemNumber).foreach(l => bw.write(l.toCsv + "\n"))
    bw.close()
  }

  private def getLineItems: Seq[ShortLineItem] = {
    Source.fromFile(csvInFile).getLines().toSeq.tail.map(ShortLineItem.fromCsv)
  }

  case class ShortLineItem(lineItemNumber: Int,
                           oldSlotId: Long,
                           websiteName: String,
                           websiteId: Option[Long],
                           websiteUrl: Option[String],
                           whitelistedDomains: Option[String],
                           slotName: String,
                           newSlotId: Option[Long],
                           accountID: Long) {
    def toCsv: String = {
      s"$oldSlotId,$websiteName,${websiteId.get},${websiteUrl.get},${whitelistedDomains.get},$slotName,${newSlotId.get}"
    }
  }

  object ShortLineItem {
    def fromCsv(record: String): ShortLineItem = {
      println(record)
      val fields = record.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)")
      ShortLineItem(fields(0).toInt, fields(1).toLong, fields(2), None, None, None, fields(3), None, seedingAllianceAccountId)
    }
  }

  case class Website(id: Long, name: String, url: String, whitelistedDomains: String)

}
