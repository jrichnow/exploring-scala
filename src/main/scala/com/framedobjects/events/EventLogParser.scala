package com.framedobjects.events

import com.framedobjects.events.model.{EventLogEntry, EventTypeLogEntry, ServerLogEntry}

import scala.io.Source
object EventLogParser {

  def main(args: Array[String]) {
    val eventLog = Source.fromFile("/users/jensr/Documents/DevNotes/investigations/im-events/server.json.20160908.log").getLines().toSeq
    println(s"${eventLog.size} total log entries")

    // Get the event types first.
    val eventTypes = eventLog.filter(_.contains("eventType"))
    val jsonEventTypes = eventTypes.map(ServerLogEntry.fromJson(_))
    println(s"${jsonEventTypes.size} json event types")
    val jsonEventTypesById = jsonEventTypes.map(json => (json.data.reqBody.id, json))
    jsonEventTypesById.take(10).foreach(println)
//    val logIds = jsonEventTypes.map(_.reqBody.id)

    val postEvents = eventLog.filter(_.contains("POST")).filter(_.contains("/entity-event")).filter(_.contains("statusCode"))
    val jsonPostEvents = postEvents.map(EventLogEntry.fromJson(_))
    println(s"${jsonPostEvents.size} json POST events")
    jsonPostEvents.take(10).foreach(println)
    val jsonPostEventsById = jsonPostEvents.map(json => (json.req.id, json))
    jsonPostEventsById.take(10).foreach(println)
    jsonPostEventsById.intersect(jsonEventTypesById).take(10).foreach(println)

//
//    val postEventIds = jsonPostEvents.map(_.req.id)
//    postEventIds.take(5).foreach(println)
//
//    val eventTypes = eventLog.filter(_.contains("eventType")).filter(filterById(_, postEventIds))
//    println(s"${eventTypes.size} total event typrs for ${postEventIds.size} unique Ids")
//    eventTypes.take(5).foreach(println)
  }

  private def filterById(log: String, ids: Seq[String]): Boolean = {
    for (id <- ids) {
      if (log.contains(id)) return true
    }
    return false
  }

}
