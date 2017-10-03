package com.framedobjects.sql

import scala.io.Source

object WebsiteUpdater {

  def main(args: Array[String]): Unit = {
    val csv = Source.fromFile("/users/jensr/Documents/DevNotes/investigations/im-events/20161024/import_id_mapping.csv").getLines().toSeq
    println(s"${csv.size} websites")

    val websites = csv.tail.map(Website.fromCsv(_))

    for(website <- websites) {
      println(s"${website.adscaleId} - select website_id, name, url from website where fk_account_id = 4509 and name = '${website.name}' and url = '${website.url}';")
    }

    for(website <- websites) {
      println(s"update website set ibillboard_id = '${website.iBillboardId}', interactive_media_id = '${website.iamId}' where website_id=${website.adscaleId};")
    }
  }

  case class Website(adscaleId: String, iBillboardId: String, iamId: String, name: String, url: String)
  object Website {
    def fromCsv(line: String): Website = {
      val data = line.split(";")
      Website(data(0), data(1), data(2), data(3), data(4))
    }
  }
}
