package com.framedobjects.db

import scala.io.Source

object CompareDbExport {

  def main(args: Array[String]): Unit = {
    val slotInteractiveMediaIds = getInteractiveMediaIdList("/users/jensr/Documents/DevNotes/investigations/im-events/slot.csv")
    println(slotInteractiveMediaIds.size)

    val slotCloneInteractiveMediaIds = getInteractiveMediaIdList("/users/jensr/Documents/DevNotes/investigations/im-events/slot_clone.csv")
    println(slotCloneInteractiveMediaIds.size)

    val diff = slotCloneInteractiveMediaIds.diff(slotInteractiveMediaIds)
    diff.foreach(println)
  }

  private def getInteractiveMediaIdList(file: String): Seq[String] = {
    val slots = Source.fromFile(file).getLines().toSeq.tail
    slots.map { s => s.split(",")(2) }
  }
}