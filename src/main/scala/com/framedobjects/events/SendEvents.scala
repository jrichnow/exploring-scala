package com.framedobjects.events

import com.framedobjects.events.model.{EventType, EventTypeLogEntry, ServerLogEntry}
import com.ning.http.client.Response
import dispatch.{Future, Http, url}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.Source
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object SendEvents {

  def main(args: Array[String]) {
//    singleEvent()
//    eventList()
    sendIbillboardRequest()
  }

  private def sendIbillboardRequest() {
    val json = "{\"lastSeenEventRevision\":{\"transactionId\":374816,\"eventIdInTransaction\":3,\"eventCount\":5}}"

    val sendFromStart = "sendFromStart"
    val stop = "stop"
    val send = "send" // requires body
    val resume = "resume"

    val bearerStagingEnv = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJsZWdhbFBlcnNvbklkIjoiMWY3M2E2NmEtZjA2Yi00M2ViLWI5ZTAtN2M4ODA3NzY3Njk3Iiwic3lzdGVtIjp7ImlkIjoiMzhjYTViNzgtM2RjNS00ZGQ3LWI2MGQtNGJhYzIwMGYzNGJmIiwicmVwcmVzZW50cyI6eyJhZjllZmQxMC04NDBiLTQ3NTctOTQ3Yy1lZThkODVlNzc4ZDIiOnsiYWY5ZWZkMTAtODQwYi00NzU3LTk0N2MtZWU4ZDg1ZTc3OGQyIjp7ImlkIjoiYWY5ZWZkMTAtODQwYi00NzU3LTk0N2MtZWU4ZDg1ZTc3OGQyIiwicm9sZXMiOlsiZXZlbnRTdHJlYW0iXX19fSwiY29uZmlybWVkIjp0cnVlfSwiaWF0IjoxNDY4MzkzNjcxLCJhdWQiOiI2SGluMVVPN0h6U3ZvTVhRcElsc3daWHFibFdoY0huTyJ9.FeF6WP9pVP9WzNyV4eFw5KsuVgxlS_xaeRALMFKTKYQ"
    val bearerProductionEnv = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJsZWdhbFBlcnNvbklkIjoiMWY3M2E2NmEtZjA2Yi00M2ViLWI5ZTAtN2M4ODA3NzY3Njk3Iiwic3lzdGVtIjp7ImlkIjoiMzhjYTViNzgtM2RjNS00ZGQ3LWI2MGQtNGJhYzIwMGYzNGJmIiwicmVwcmVzZW50cyI6eyI0NTI3MzI3Ny1hM2Y5LTQwODYtYmE0MC04ZDhjZWQ5MjQ5OWIiOnsiNDUyNzMyNzctYTNmOS00MDg2LWJhNDAtOGQ4Y2VkOTI0OTliIjp7ImlkIjoiNDUyNzMyNzctYTNmOS00MDg2LWJhNDAtOGQ4Y2VkOTI0OTliIiwicm9sZXMiOlsiZXZlbnRTdHJlYW0iXX19fSwiY29uZmlybWVkIjp0cnVlfSwiaWF0IjoxNDY4MzkzNjcxLCJhdWQiOiI2SGluMVVPN0h6U3ZvTVhRcElsc3daWHFibFdoY0huTyJ9.PY9aiB19pqqwh0iiq0U_pSJjc1yDuoaahijs5zLNkcE"

    val stagingUrl = "clientui-mbrdsp-staging.ibillboard.com"
    val productionUrl = "clientui-mbrdsp.ibillboard.com"

    val response = Http(
      url(s"https://$productionUrl/api/eventStream/$send")
        .POST
        .setBody(json)
        .setBodyEncoding("UTF-8")
        .setHeader("Content-Type", "application/json")
        .setHeader("Authorization", s"Bearer $bearerProductionEnv"))

    val resp = response.map(r => r.getResponseBody)
    val result = Await.result(resp, 10.second)

    println(result)
  }

  private def sendEvent(event: String): Future[Int] = {
    val response = Http(
      url("http://localhost:8000/entity-event")
        .POST
        .setHeader("Content-Type", "application/json")
        .setBody(event))

    response.map(r => {
      println(r.getResponseBody)
      r.getStatusCode})
  }

  private def singleEvent(): Unit = {
    val event =
      """
        {"eventType":"NewOperatorAdded","metadata":{"created":1435212504833,"invalid":false,"origin":"backend","transaction":{"transactionId":1,"eventIdInTransaction":1,"eventCountInTransaction":2}},"data":{"operator":{"name":"MBR operator","email":"operator@mbr-example.org","id":"2bc15d32-7ff4-452e-ad4e-07c9c63552fe","isReseller":null}}}
      """

    val responseCode = sendEvent(event)
    val status = Await.result(responseCode, 10.second)
    println(s"$status - ${EventType.fromJson(event)}")
  }

  private def eventList(): Unit = {
//    val events = Source.fromFile("/users/jensr/Documents/DevNotes/investigations/im-events/20160808/inventory.event.json").getLines().toList
    val events = Source.fromFile("/users/jensr/Documents/DevNotes/investigations/im-events/events-201609023.json").getLines().toList
    println(s"loaded ${events.size} server log entries")

    println(events.filter(_.contains("WebsiteCreated")).size)
    println(events.filter(_.contains("AdSlotCreated")).size)

    try {
      for (event <- events) {
        val responseCode = sendEvent(event)
        val status = Await.result(responseCode, 10.second)
        println(s"$status - ${EventType.fromJson(event)}")
        //        if (status >= 400) {
        //          return new RuntimeException("that's it")
        //        }
      }
    } catch {
      case t: Throwable => println(t.getMessage)
    }
  }
}