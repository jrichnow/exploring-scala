package com.framedobjects.events.model

import net.liftweb.json.{DefaultFormats, _}

case class Wrapper(time: Long, msg: EventType)

case class EventType(eventType: String, data: EventTypeData) {
  override def toString: String = {
    eventType match {
      case websiteCreated if (websiteCreated.equals("WebsiteCreated")) => {s"$eventType, type: ${data.entityType.get}, name: ${data.name.get}, id: ${data.entityId.get.uuid}, url: ${data.url.get}"}
      case adSlotCreated if (adSlotCreated.equals("AdSlotCreated")) => {s"$eventType, name: ${data.name.get}, id: ${data.entityId.get.uuid}, alias: ${data.alias.get}, websiteId: ${data.websiteId.get.uuid}"}
      case formatAdded if (formatAdded.equals("SupportedFormatAddedToAdSlot")) => {s"$eventType, formats: ${data.adFormats.get.head}, entityId: ${data.entityId.get.uuid}"}
      case _ => {s"$eventType"}
    }
  }
}
case class EventTypeData(entityId: Option[EntityId2],
                         entityType: Option[String],
                         name: Option[String],
                         url: Option[String],
                         alias: Option[String],
                         websiteId: Option[EntityId2],
                         adFormats: Option[Seq[AdFormat]])
case class EntityId2(uuid: String, externalIds: Option[ExternalIds])
case class ExternalIds(InteractiveMedia: String)
case class AdFormat(name: String, dimensions: AdFormatDimension)
case class AdFormatDimension(width: Int, height: Int)


object EventType {

  implicit val formats = DefaultFormats

  def fromJson(jsonString: String): EventType = {
    val jValue = parse(jsonString)
    jValue.extract[EventType]
  }

  def wrapperFromJson(jsonString: String): Wrapper = {
    val jValue = parse(jsonString)
    jValue.extract[Wrapper]
  }
}
