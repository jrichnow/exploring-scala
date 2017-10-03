package com.framedobjects.events.model

import net.liftweb.json._

case class EventLogEntry(time: Long,
                         res: EventLogResponse,
                         req: EventLogRequest)

case class EventLogResponse(statusCode: Int)

case class EventLogRequest(id: String)

object EventLogEntry {

  implicit val formats = DefaultFormats

  def fromJson(jsonString: String): EventLogEntry = {
    val jValue = parse(jsonString)
    jValue.extract[EventLogEntry]
  }
}