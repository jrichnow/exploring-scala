package com.framedobjects.misc

import scala.collection.mutable.ListBuffer
import scala.io.Source

object Histo {

  def main(args: Array[String]): Unit = {
    val csv = Source.fromFile("/users/jensr/Temp/kibana/histo.csv").getLines().toSeq
    csv.foreach(println)

    val arrays = csv.map(_.split(","))

    for (i <- (0 to 100)) {
      var line = new ListBuffer[String]
      for (array <- arrays) {
        line += array(i)
      }
      println(f"$i%3s" + line.map(n => f"$n%6s").mkString(" "))
    }
  }
}