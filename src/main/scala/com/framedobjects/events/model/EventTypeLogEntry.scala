package com.framedobjects.events.model

import net.liftweb.json._

case class ServerLogEntry(data: EventTypeLogEntry)

case class EventTypeLogEntry(reqBody: RequestBody)

case class RequestBody(id: String, payload: Payload)

case class Payload(eventType: String, data: Option[EventData])

case class EventData(entityId: EntityId)

case class EntityId(uuid: String)

object ServerLogEntry {

  implicit val formats = DefaultFormats

  def fromJson(jsonString: String): ServerLogEntry = {
    val jValue = parse(jsonString)
    jValue.extract[ServerLogEntry]
  }
}