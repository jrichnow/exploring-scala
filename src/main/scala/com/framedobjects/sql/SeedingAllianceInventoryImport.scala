package com.framedobjects.sql

import java.io.{BufferedWriter, File, FileWriter, PrintWriter}

import scala.io.Source
import java.sql.{Connection, Date, DriverManager, Statement}

import au.com.bytecode.opencsv.CSVWriter

import scala.collection.mutable.ListBuffer

object SeedingAllianceInventoryImport {

  val csvInFile = "/users/jensr/Temp/SA-modified.csv"
  val csvOutFile = "/users/jensr/Temp/SA-modified-updated.csv"

  val dbDriver = "org.postgresql.Driver"
  val dbUrl = "jdbc:postgresql://db01:5432/adscale"
  val dbUsername = "adscale"
  val dbPassword = "s0ci4lw3lfar3"

  var connection:Connection = null

  def main(args: Array[String]): Unit = {

    Class.forName(dbDriver)
    connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)

    val lineItems = getLineItems()
    println(s"${lineItems.size} tuples")

    // websites
    // check whether account exists
    connection.prepareStatement("begin transaction").execute()

    var updatedLineItems = Seq[LineItem]()

    for (lineItem <- lineItems) {
      println(s">> LineItem $lineItem")
      lineItem.sspWebsiteId match {
        case id: Some[Long] => // Check that we have a website with that ID
          if (websiteExistsById(id.get)) {
            println(s"++++ website exists for $id.get")
            val slotId = createSlot(id.get, lineItem.sspSlotName)
            println(s"slot ID: $slotId")

            val updatedLineItem = lineItem.copy(sspSlotId = Some(slotId))
            println(updatedLineItem)
            updatedLineItems = updatedLineItems :+ updatedLineItem
          }
          else {
            println(s"===========================")
          }
        case None =>
          // Check whether website exists by name
          val websiteIdOption = lineItem.sspWebsiteName match {
            case websiteName: Some[String] =>
              websiteIdByName(websiteName.get)
            case None => None
          }
          val websiteId = websiteIdOption match {
            case id: Some[Long] => id.get
            case None =>
              createWebsite(lineItem)
          }
          println(s"have website with id $websiteId")
          val slotId = createSlot(websiteId, lineItem.sspSlotName)
          println(s"slot ID: $slotId")

          val updatedLineItem = lineItem.copy(sspWebsiteId = Some(websiteId), sspSlotId = Some(slotId))
          updatedLineItems = updatedLineItems :+ updatedLineItem

      }
    }

    connection.prepareStatement("commit").execute()

    writeCsv(updatedLineItems)
  }

  private def writeCsv(lineItems: Seq[LineItem]): Unit = {
    lineItems.foreach(println(_))
    // TODO write to csv
    lineItems.sortBy(_.naturalLineNumber).foreach(l => println(l.toCsv()))

    val bw = new BufferedWriter(new FileWriter(csvOutFile))

    val header = Seq("", "", "SlotiD", "Website", "Info", "cleanname", "Account", "account ID", "WebsiteID",
      "Slotname", "Website_name", "Adscale Slot ID").mkString(",")
    bw.write(header.toString + "\n")
    lineItems.sortBy(_.naturalLineNumber).foreach(l => bw.write(l.toCsv() + "\n"))
    bw.close()
  }

  private def getLineItems(): Seq[LineItem] = {
    val csv = Source.fromFile(csvInFile).getLines().toSeq
    println(csv.head)
    println(s"${csv.size} entries")

    csv.tail.map(LineItem.fromCsv(_))
  }

  private def websiteExistsById(id: Long): Boolean = {
    val statement = connection.createStatement()
    val resultSet = statement.executeQuery("SELECT count(*) FROM website WHERE website_id=" + id)
    resultSet.next()
    resultSet.getInt(1) > 0
  }

  private def websiteIdByName(name: String): Option[Long] = {
    if (name.trim.length == 0) {
      println("=============empty website name --------------------------")
    }
    val statement = connection.createStatement()
    val resultSet = statement.executeQuery(s"SELECT website_id FROM website WHERE name='$name'")
    var websiteId: Option[Long] = None
    if (resultSet.next()) {
      websiteId = Some(resultSet.getLong(1))
    }
    else {
      websiteId = None
    }
    websiteId
  }

  private def createSlot(websiteId: Long, slotName: String): Long = {
    println(s"creating backfill for slot $slotName for website $websiteId")
    val insertBackfill = "insert into backfill (id, html, is_used) values (nextval('backfill_seq'), '', false)"
    connection.prepareStatement(insertBackfill).executeUpdate()

    println(s"creating a slot $slotName for website $websiteId")
    val insertSlot = "insert into slot (id, name, fk_website_id, fk_backfill_id, list_price_cpm, min_price_cpm, " +
      "created_date, ad_dimension, mod_time, slot_type) " +
      "values (nextval('slot_seq'), ?, ?, currval('backfill_seq'), ?, ?, now(), ?, now(), ?)"
    val slotStatement = connection.prepareStatement(insertSlot)
    slotStatement.setString(1, slotName)
    slotStatement.setLong(2, websiteId)
    slotStatement.setFloat(3, 0.05f)
    slotStatement.setFloat(4, 0.05f)
    slotStatement.setString(5, "NATIVE")
    slotStatement.setString(6, "NATIVE")

    val updatedRows = slotStatement.executeUpdate()
    // TODO check that we did an update before continuing?

    val slotIdRs = connection.createStatement().executeQuery("select currval('slot_seq')")
    slotIdRs.next()
    val slotId = slotIdRs.getLong(1)

    println(s"creating margin_slot $slotName ($slotId) for website $websiteId")
    val insertMargin = "insert into margin_slot (slot_id, date_created, publisher_margin, admin_name, admin_id, ip) " +
      "values (currval('slot_seq'), now(), ?, ?, ?, ?)"
    val marginStatement = connection.prepareStatement(insertMargin)
    marginStatement.setFloat(1, 0.90f)
    marginStatement.setString(2, "manual native slot import")
    marginStatement.setLong(3, 3082L)
    marginStatement.setString(4, "127.0.0.1")

    marginStatement.executeUpdate()

    slotId
  }

  private def createWebsite(lineItem: LineItem): Long = {
    println(s"creating website ${lineItem.sspWebsiteName.get}")
    val whitelistedReferrer = lineItem.sspWebsiteName match {
      case name: Some[String] if name.get.startsWith("SA-") => name.get.substring(3)
      case any: Some[String] => any.get
      case _ => "should not happen"
    }

    val insertWebsite = "insert into website (website_id, name, url, fk_account_id, created_date, mod_time, category, " +
      "white_listed_domains, channel_url) values (nextval('website_seq'), ?, ?, ?, now(), now(), ?, ?, ?)"
    val websiteStatement = connection.prepareStatement(insertWebsite)
    websiteStatement.setString(1, lineItem.sspWebsiteName.get)
    websiteStatement.setString(2, lineItem.saWebsiteUrl)
    websiteStatement.setLong(3, lineItem.sspAccountId)
    websiteStatement.setString(4, "HOME")
    websiteStatement.setString(5, whitelistedReferrer)
    websiteStatement.setString(6, lineItem.saWebsiteUrl)

    websiteStatement.executeUpdate()

    val websiteIdRs = connection.createStatement().executeQuery("select currval('website_seq')")
    websiteIdRs.next()
    val websiteId = websiteIdRs.getLong(1)

    val selectDefaultFilterId = "SELECT id FROM publisher_auto_approval_filter WHERE account_id = ?"
    val filterIdStatement = connection.prepareStatement(selectDefaultFilterId);
    filterIdStatement.setLong(1, lineItem.sspAccountId)
    val filterIdRs = filterIdStatement.executeQuery()
    while (filterIdRs.next()) {
      val filterId = filterIdRs.getLong(1)
      println(s"creating publisher_filter_to_website for website ID $websiteId and filterId $filterId")

      val insertFilter = "INSERT INTO publisher_filter_to_website (website_id, filter_id) VALUES (?, ?)"
      val filterStatement = connection.prepareStatement(insertFilter)
      filterStatement.setLong(1, websiteId)
      filterStatement.setLong(2, filterId)

      filterStatement.executeUpdate()
    }

    websiteId
  }

  case class LineItem(naturalLineNumber: Int,
                      specifiedLineNumber: Int,
                      saSlotId: String,
                      saWebsiteUrl: String,
                      saInfo: String,
                      saCleanName: String,
                      sspAccountName: String,
                      sspAccountId: Long,
                      sspWebsiteId: Option[Long],
                      sspSlotName: String,
                      sspWebsiteName: Option[String],
                      sspSlotId: Option[Long]
                     ) {

    def toCsv(): String = {
      s"$naturalLineNumber,$specifiedLineNumber,$saSlotId,$saWebsiteUrl,$saInfo,$saCleanName," +
        s"$sspAccountName,$sspAccountId,${sspWebsiteId.get},$sspSlotName,${sspWebsiteName.getOrElse("")},${sspSlotId.get}"
    }

    def toStringArray(): Array[String] ={
      Array(naturalLineNumber.toString, specifiedLineNumber.toString ,saSlotId, saWebsiteUrl, saInfo, saCleanName,
        sspAccountName,sspAccountId.toString, sspWebsiteId.get.toString,sspSlotName,sspWebsiteName.getOrElse(""),
        sspSlotId.get.toString)
    }
  }

  object LineItem {

    def fromCsv(record: String): LineItem = {
      val d = record.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)")

      val sspWebsiteId = d(8) match {
        case empty if empty.length == 0 => None
        case _ => Some(d(8).toLong)
      }

      val sspWebsiteName = d match {
        case a if a.length == 10 => None
        case b if b.length == 11 => Some(d(10))
      }

      LineItem(d(0).toInt, d(1).toInt, d(2), d(3), d(4), d(5), d(6), d(7).toLong, sspWebsiteId, d(9), sspWebsiteName, None)
    }


  }
}