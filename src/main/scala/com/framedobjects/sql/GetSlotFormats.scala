package com.framedobjects.sql

import java.sql.{Connection, DriverManager}

import org.apache.commons.codec.binary.StringUtils

object GetSlotFormats {

  def main(args: Array[String]): Unit = {
    implicit val connection: Connection = getDbConnection

    val slotIds = Seq(189627,189584,189585,187302,187868,187865,187869,187753,187754,187300,188988,191468,189600,189601,187867,190897,176186,112516,188947,188943,188948,187292,39586,187294).sorted
    slotIds.foreach(getSlotFormat(_))
  }


  private def getDbConnection: Connection = {
    val dbDriver = "org.postgresql.Driver"

    val dbUrl = "jdbc:postgresql://localhost:5432/adscale"
    val dbUsername = ""
    val dbPassword = ""

    Class.forName(dbDriver)
    DriverManager.getConnection(dbUrl, dbUsername, dbPassword)
  }

  def getSlotFormat(slotId:Long)(implicit conn: Connection): Unit = {
    val statement = conn.createStatement()
    val resultSet = statement.executeQuery(s"select id, ad_dimension, array_to_string(array(select ad_dimension from slot_extra_formats where slot_id = $slotId), ',') as additional_formats from slot where id=$slotId")
    resultSet.next()
    val sid = resultSet.getLong(1)
    val defaultFormat = resultSet.getString(2)
    val extraFormats = resultSet.getString(3)
    if (extraFormats.length > 0) {
      println(s"$sid: $defaultFormat,$extraFormats")
    } else {
      println(s"$sid: $defaultFormat")
    }
  }

}
