package com.framedobjects.sql

import java.sql.{Connection, DriverManager}

object DbUtil {

  def getDbConnection: Connection = {
    val dbDriver = "org.postgresql.Driver"

    val dbUrl = "jdbc:postgresql://10.14.15.64:5432/adscale_pte"
    val dbUsername = "adscale"
    val dbPassword = "mmmbeer"

    Class.forName(dbDriver)
    DriverManager.getConnection(dbUrl, dbUsername, dbPassword)
  }

  def transaction(sqlStatements: => Unit)(implicit conn: Connection, commit: Boolean) {
    conn.prepareStatement("begin transaction").execute()

    sqlStatements

    if (commit) {
      println("commit")
      conn.prepareStatement("commit").execute()
    } else {
      println("rollback")
      conn.prepareStatement("rollback").execute()
    }
  }

  def websiteExistsById(id: Long)(implicit conn: Connection): Boolean = {
    val statement = conn.createStatement()
    val resultSet = statement.executeQuery("SELECT count(*) FROM website WHERE website_id=" + id)
    resultSet.next()
    resultSet.getInt(1) > 0
  }

  def createSlot(websiteId: Long, slotName: String)(implicit conn: Connection): Long = {
    println(s"creating backfill for slot $slotName for website $websiteId")
    val insertBackfill = "insert into backfill (id, html, is_used) values (nextval('backfill_seq'), '', false)"
    conn.prepareStatement(insertBackfill).executeUpdate()

    println(s"creating a slot $slotName for website $websiteId")
    val insertSlot = "insert into slot (id, name, fk_website_id, fk_backfill_id, list_price_cpm, min_price_cpm, " +
      "created_date, ad_dimension, title_color, text_color, background_color, border_color, mod_time, slot_type) " +
      "values (nextval('slot_seq'), ?, ?, currval('backfill_seq'), ?, ?, now(), ?, ?, ?, ?, ?, now(), ?)"
    val slotStatement = conn.prepareStatement(insertSlot)
    slotStatement.setString(1, slotName)
    slotStatement.setLong(2, websiteId)
    slotStatement.setFloat(3, 0.05f)
    slotStatement.setFloat(4, 0.05f)
    slotStatement.setString(5, "NATIVE")
    slotStatement.setString(6, "#0000FF")
    slotStatement.setString(7, "#000000")
    slotStatement.setString(8, "#FFFFFF")
    slotStatement.setString(9, "#000000")
    slotStatement.setString(10, "NATIVE")

    slotStatement.executeUpdate()

    val slotIdRs = conn.createStatement().executeQuery("select currval('slot_seq')")
    slotIdRs.next()
    val slotId = slotIdRs.getLong(1)

    println(s"creating margin_slot $slotName ($slotId) for website $websiteId")
    val insertMargin = "insert into margin_slot (slot_id, date_created, publisher_margin, admin_name, admin_id, ip) " +
      "values (currval('slot_seq'), now(), ?, ?, ?, ?)"
    val marginStatement = conn.prepareStatement(insertMargin)
    marginStatement.setFloat(1, 0.90f)
    marginStatement.setString(2, "manual native slot import")
    marginStatement.setLong(3, 3082L)
    marginStatement.setString(4, "127.0.0.1")

    marginStatement.executeUpdate()

    slotId
  }

}
