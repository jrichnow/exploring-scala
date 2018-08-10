package com.framedobjects.sql

import java.sql.Connection

object SeedingAllianceSetFilterForWebsite {

  private val accountId = 17229
  private val filterName = "OMS Netzwerk allgemein"

  def main(args: Array[String]): Unit = {
    implicit val connection: Connection = DbUtil.getDbConnection
    implicit val doCommit: Boolean = true

    DbUtil.transaction {
      val websites = findWebsitesWithoutFilterForAccount()
      println(s"Found ${websites.size} websites without a filter")
      websites.foreach(println)

      val filterIdOption = getFilterIdByNameForAccount(filterName)
      filterIdOption match {
        case id: Some[Long] => {
          println(s"filter id for $filterName is ${id.get}")
          assignWebsitesToFilter(websites, id.get)

          val sanityCheck = findWebsitesWithoutFilterForAccount()
          println(s"After assignment found ${sanityCheck.size} websites without a filter")
          if (sanityCheck.isEmpty) {
            generateBeanWorksStatements(websites)
          }
        }
        case None => println("No filter found")
      }
    }
  }

  private def findWebsitesWithoutFilterForAccount()(implicit conn: Connection): Seq[Website] = {
      val statement = conn.createStatement()
      val resultSet = statement.executeQuery(
        s"select ws.website_id, ws.name from website ws " +
          s"left join publisher_filter_to_website pftw using (website_id) " +
          s"where ws.fk_account_id = $accountId and ws.is_active is true and pftw.website_id is null")

      var websites = Seq[Website]()
      while (resultSet.next()) {
        val website = Website(resultSet.getLong(1), resultSet.getString(2))
        websites = websites :+ website
      }
    websites
  }

  private def getFilterIdByNameForAccount(filterName: String)(implicit conn: Connection): Option[Long] = {
    val statement = conn.createStatement()
    val resultSet = statement.executeQuery(
      s"select id from publisher_auto_approval_filter where account_id=$accountId and name='$filterName'")
    if (resultSet.next()) {
      Some(resultSet.getLong(1))
    }
    else {
      None
    }
  }

  private def assignWebsitesToFilter(websites: Seq[Website], filterId: Long)(implicit conn: Connection): Unit = {
    websites.foreach {x =>
      val insertSql = s"insert into publisher_filter_to_website (website_id, filter_id) values (${x.id}, $filterId)"
      println(insertSql)
      conn.prepareStatement(insertSql).executeUpdate()
    }
  }

  private def generateBeanWorksStatements(websites: Seq[Website]) = {
    websites.foreach{x =>
      println(s"beanWorks.pl -t market  m Adscale:type=Market,name=MarketNotificationMBean sendWebsiteNotification ${x.id}")
    }
  }

  case class Website(id: Long, name: String)
}