package com.framedobjects.sql

import java.sql.Connection

object ExportSlotsHavingSoftFloor {

  def main(args: Array[String]): Unit = {
    implicit val connection = DbUtil.getPostgresDbConnection
    val slotInfos = getSlotsWithSoftFloor()

    println(s"got ${slotInfos.size} infos")
    slotInfos.foreach(println)
  }

  private def getSlotsWithSoftFloor()(implicit conn: Connection): Seq[SlotInfo] = {
    val query =
      """
        |select a.user_name as "account name", a.id as "account name", w.name as "website name", w.website_id as "website id",
        |s.name as "slot name", s.id as "slot id", soft_floor_percentage, s.ad_dimension
        |from account a
        |join website w on a.id=w.fk_account_id
        |join slot s on s.fk_website_id = w.website_id
        |where s.id in (
        |     select slot.id from slot
        |     join website w on w.website_id = slot.fk_website_id
        |     join account a on a.id = w.fk_account_id
        |     where slot.is_active is true and w.is_active is true and a.is_disabled is false
        |         and soft_floor_percentage > 0)
        |    order by a.user_name, w.name, soft_floor_percentage desc;
      """.stripMargin
    val statement = conn.createStatement()
    val rs = statement.executeQuery(query)

    var slotInfos = Seq[SlotInfo]()
    while (rs.next()) {
      val slotInfo = SlotInfo(rs.getString(1), rs.getLong(2), rs.getString(3),
        rs.getLong(4), rs.getString(5), rs.getLong(6), rs.getFloat(7),
        rs.getString(8))
      println(slotInfo)
      slotInfos = slotInfos :+ slotInfo
    }
    slotInfos
  }

  case class SlotInfo(accountName: String,
                      accountId: Long,
                      websiteName: String,
                      websiteId: Long,
                      slotName: String,
                      slotId: Long,
                      softFloorPercentage: Float,
                      dimension: String)

}
