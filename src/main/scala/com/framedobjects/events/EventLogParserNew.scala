package com.framedobjects.events

import com.framedobjects.events.model.ServerLogEntry

import scala.io.Source

object EventLogParserNew {

  def main(args: Array[String]): Unit = {
    val eventLog = Source.fromFile("/users/jensr/Documents/DevNotes/investigations/im-events/server.json.20160908a.log").getLines().toSeq

    val eventTypeLogs = eventLog.filter(_.contains("eventType"))
    val eventTypes = eventTypeLogs.map(ServerLogEntry.fromJson(_))
    val distinctEventTypes = eventTypes.map(_.data.reqBody.payload.eventType).distinct.sorted

    distinctEventTypes.foreach(println)
  }

}