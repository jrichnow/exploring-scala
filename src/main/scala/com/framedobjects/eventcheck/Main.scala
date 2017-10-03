package com.framedobjects.eventcheck

import com.framedobjects.events.model.{AdFormat, EventType, EventTypeData}

import scala.collection.immutable.HashMap
import scala.io.Source

object Main {

  def main(args: Array[String]): Unit = {
    compareEntities()
  }

  private def compareEntities(): Unit = {
    val (eventWebsites, eventSlots) = getEntitiesFromEvents()
    println(s"${eventWebsites.size} websites from events")

    val dbWebsites = getWebsitesFromDbCsv()
    println(s"${dbWebsites.size} websites from db")

    println(s"${eventSlots.size} slots from events")
    eventSlots.take(10).foreach(println)

    val dbSlots = getSlotsFromDbCsv()
    println(s"${dbSlots.size} slots from db")
    dbSlots.take(10).foreach(println)

    val zeroSlotWebsites = eventWebsites.values.filter(_.slots.size == 0).size
    println(s"$zeroSlotWebsites zero slot event website")

    val updatedDbWebsites = addDbSlotsToDbWebsites(dbWebsites, dbSlots)

    val zeroSlotDbWebsites = updatedDbWebsites.values.filter(_.slots.size == 0).size
    println(s"$zeroSlotDbWebsites zero slot db website")

    for (websiteEntry <- eventWebsites) {
      val dbWebsiteOption = updatedDbWebsites.get(websiteEntry._1)
      val eventWebsite = websiteEntry._2
      if (!websiteEquals(eventWebsite, dbWebsiteOption.get)) {
        println(s"${eventWebsite}\n${dbWebsiteOption.get}\n")
      }
    }

    for (slotEntry <- eventSlots) {
      val dbSlotOption = dbSlots.get(slotEntry._1)
      val eventSlot = slotEntry._2
      if (!slotEquals(eventSlot, dbSlotOption.get)) {
        println(s"${eventSlot}\n${dbSlotOption.get}\n")
      }
    }
  }

  private def getWebsitesFromDbCsv(): HashMap[String, Website] = {
    val csvDbEvents = Source.fromFile("/Users/jensr/Documents/DevNotes/investigations/im-events/20160808/website_clone.csv").getLines().toList.drop(1)
    val websites = csvDbEvents.map(_.split(",")).map(createWebsiteFromCsv(_))

    var websitesMap = HashMap.empty[String, Website]
    for (website <- websites) {
      websitesMap += (website.uuid -> website)
    }
    websitesMap
  }

  private def getSlotsFromDbCsv(): HashMap[String, Slot] = {
    val csvDbEvents = Source.fromFile("/Users/jensr/Documents/DevNotes/investigations/im-events/20160808/slot_clone.csv").getLines().toList.drop(1)
    val slots = csvDbEvents.map(_.split(",")).map(createSlotFromCsv(_))

    var slotsMap = HashMap.empty[String, Slot]
    for (slot <- slots) {
      slotsMap += (slot.uuid -> slot)
    }
    slotsMap
  }

  private def getAdDimensions(): Seq[AdDimension] = {
    val csvAdDimensions = Source.fromFile("/Users/jensr/Documents/DevNotes/investigations/im-events/20160808/ad_dimension_lookup.csv").getLines().toList.drop(1)
    csvAdDimensions.map(_.split(",")).map(createAdDimensionFromCsv(_))
  }

  private def getEntitiesFromEvents(): (HashMap[String, Website], HashMap[String, Slot]) = {
    val jsonEvents = Source.fromFile("/Users/jensr/Documents/DevNotes/investigations/im-events/20160808/event.json.log").getLines().toList
    val events = jsonEvents.map(EventType.wrapperFromJson(_)).filter(_.time >= 1470397063575l).map(_.msg)
    val adDimensions = getAdDimensions()

    var eventTypes = Set.empty[String]
    var websites = HashMap.empty[String, Website]
    var slots = HashMap.empty[String, Slot]

    for (event <- events) {
      eventTypes += event.eventType
      event.eventType match {
        case "WebsiteCreated" => val website = createWebsiteFromEvent(event.data)
          if (!websites.contains(website.uuid)) {
            websites += (website.uuid -> website)
          }
        case "AdSlotCreated" => val slot = createSlotFromEvent(event.data)
          if (!slots.contains(slot.uuid)) {
            slots += (slot.uuid -> slot)

          val websiteOption = websites.get(slot.websiteId)
          websiteOption match {
            case None => println(s"did not find website to attach slot to. $slot")
            case Some(website) => val slotRefs = website.slots :+ slot
              val newWebsite = website.copy(slots = slotRefs)
              //              println(s"adding slot to website: ${website.slots.size} --> ${newWebsite.slots.size}")
              websites += (newWebsite.uuid -> newWebsite)
          }
          }
        case "SupportedFormatAddedToAdSlot" =>
          val slotId = event.data.entityId.get.uuid
          val slotOption = slots.get(slotId)
          val slot = addAdDimensionToSlot(slotOption.get, event.data, adDimensions)
          slot match {
            case s: Some[Slot] =>
              slots += (s.get.uuid -> s.get)
            case None => println(s"======================== $slotOption")
          }
        case _ =>
      }
    }
    eventTypes.toList.sorted.foreach(println)
    (websites, slots)
  }

  private def createWebsiteFromEvent(eventData: EventTypeData): Website = {
    val interactiveMediaId = eventData.entityId.get.externalIds.map(id => id.InteractiveMedia).getOrElse("")
    Website(eventData.entityId.get.uuid, eventData.name.getOrElse(""), eventData.url.getOrElse(""), interactiveMediaId, Seq.empty[Slot], None)
  }

  private def createSlotFromEvent(eventData: EventTypeData): Slot = {
    val interactiveMediaId = eventData.entityId.get.externalIds.map(id => id.InteractiveMedia).getOrElse("")
    Slot(eventData.entityId.get.uuid, eventData.name.getOrElse(""), eventData.alias.getOrElse(""), eventData.websiteId.get.uuid, interactiveMediaId, None)
  }

  private def createWebsiteFromCsv(array: Array[String]): Website = {
    Website(array(13), array(1), array(2).substring(7), array(14), Seq.empty[Slot], Some(array(0)))
  }

  private def createSlotFromCsv(array: Array[String]): Slot = {
    Slot(array(41), array(1), array(43), array(7), array(42), Some(array(21)))
  }

  private def addDbSlotsToDbWebsites(dbWebsites: HashMap[String, Website], slots: HashMap[String, Slot]): HashMap[String, Website] = {
    var websites = dbWebsites
    for (slot <- slots.values) {
//      println(slot.websiteId)
//      println(websites.values)
      val website = websites.values.filter(_.adscaleId.get == slot.websiteId).head
      val newSlots = website.slots :+ slot
      val newWebsite = website.copy(slots = newSlots)
//      println(s"adding slot to website: ${website.slots.size} --> ${newWebsite.slots.size}")
      websites += (newWebsite.uuid -> newWebsite)
    }
    websites
  }

  private def createAdDimensionFromCsv(array: Array[String]): AdDimension = {
    AdDimension(array(0), array(3).toInt, array(4).toInt)
  }

  private def addAdDimensionToSlot(slot: Slot, eventData: EventTypeData, adDimensions: Seq[AdDimension]): Option[Slot] = {
    val slotOption = eventData.adFormats match {
      case None => None
      case Some(adFormats) =>
        if (adFormats.size == 1) {
          val width = adFormats.head.dimensions.width
          val height = adFormats.head.dimensions.height
          val adDimensionName = adDimensions.filter(d => d.width == width && d.height == height).head.name
          slot.adDimension match {
            case Some(adDimension) => println(s"  XXXXXXXXXXXXXX slot has already a dimention $slot")
              None
            case None =>
              Some(slot.copy(adDimension = Some(adDimensionName)))
          }
        }
        else {
          println(s"          !!!!!!!!!!!!!!!!! $eventData")
          None
        }
    }
    slotOption
  }

  private def slotEquals(eventSlot:Slot, dbSlot:Slot): Boolean = {
    eventSlot.adDimension == dbSlot.adDimension &&
      eventSlot.alias == dbSlot.alias &&
      eventSlot.uuid == dbSlot.uuid &&
      eventSlot.name == dbSlot.name &&
      eventSlot.interactiveMediaId == dbSlot.interactiveMediaId
  }

  private def websiteEquals(eventWebsite: Website, dbWebsite: Website): Boolean = {
    eventWebsite.uuid == dbWebsite.uuid &&
    eventWebsite.name == dbWebsite.name &&
    eventWebsite.url == dbWebsite.url &&
    eventWebsite.interactiveMediaId == dbWebsite.interactiveMediaId &&
    eventWebsite.slots.size == dbWebsite.slots.size
  }

  case class Website(uuid: String, name: String, url: String, interactiveMediaId: String, slots: Seq[Slot], adscaleId: Option[String])
  case class Slot(uuid: String, name: String, alias: String, websiteId: String, interactiveMediaId: String, adDimension: Option[String])
  case class AdDimension(name: String, width: Int, height: Int)
}