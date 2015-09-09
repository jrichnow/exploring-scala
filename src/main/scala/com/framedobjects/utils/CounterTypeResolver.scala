package com.framedobjects.utils

import scala.util.Try
import scala.util.Success
import scala.util.Failure

sealed trait CounterType {
  def id: Int
  def name: String
}

case class CounterTypeImpl(id: Int, name: String) extends CounterType

object CounterTypeResolver {

  val counterTypes = Seq(
    CounterTypeImpl(0, "CPC_CLICK"),
    CounterTypeImpl(1, "CPC_TEXT_IMPRESSION"),
    CounterTypeImpl(2, "CPM_CLICK"),
    CounterTypeImpl(3, "CPM_IMPRESSION"),
    CounterTypeImpl(4, "FLATRATE_CLICK"),
    CounterTypeImpl(5, "FLATRATE_IMPRESSION"),
    CounterTypeImpl(6, "BACKFILL_IMPRESSION"),
    CounterTypeImpl(8, "ADSCALE_AD_IMPRESSION"),
    CounterTypeImpl(9, "CCH_CLICK"),
    CounterTypeImpl(10, "IMPRESSION_COMPLETION"),
    CounterTypeImpl(11, "CLICK_TRACKING"),
    CounterTypeImpl(13, "CPC_BANNER_IMPRESSION"),
    CounterTypeImpl(14, "CH_DOUBLE_CLICK_DETECTION"),
    CounterTypeImpl(15, "CCH_DOUBLE_CLICK_DETECTION"),
    CounterTypeImpl(16, "CALLBACK_ATTEMPT"),
    CounterTypeImpl(17, "CALLBACK_DECLINED"),
    CounterTypeImpl(18, "RETARGETING_PIXEL"),
    CounterTypeImpl(19, "VIDEO_CONFIGURATION"),
    CounterTypeImpl(20, "VIDEO_EVENT"),
    CounterTypeImpl(21, "VIDEO_IMPRESSION"),
    CounterTypeImpl(22, "POST_VIEW"),
    CounterTypeImpl(23, "CLICK_TRACKING_2"),
    CounterTypeImpl(24, "POST_VIEW_2"),
    CounterTypeImpl(25, "RTB_IMPRESSION"),
    CounterTypeImpl(26, "VIDEO_UNSOLD_IMPRESSION"),
    CounterTypeImpl(27, "DSP_ATTEMPT"),
    CounterTypeImpl(28, "DSP_NO_ATTEMPT"),
    CounterTypeImpl(29, "DSP_IMPRESSION"),
    CounterTypeImpl(30, "DSP_CLICK"),
    CounterTypeImpl(31, "DSP_CCH_CLICK"),
    CounterTypeImpl(32, "POPUNDER_IMPRESSION"),
    CounterTypeImpl(33, "DSP_POPUNDER_IMPRESSION"),
    CounterTypeImpl(34, "DSP_POPUNDER_COMPLETION"),
    CounterTypeImpl(35, "POPUNDER_COMPLETION"),
    CounterTypeImpl(36, "POPUNDER_ATTEMPT"))

  def main(args: Array[String]): Unit = {
    args match {
      case a if (a.length == 1) => {
        if (a(0) == "all") {
          counterTypes.foreach(t => println(s"${t.id} --> ${t.name}"))
        }
        else {
          val result = getCounterTypeFromNumber(a(0))
          result match {
            case Success(b) => println(s"${a(0)} --> $b")
            case Failure(e) => println(s"Error occurred: ${e.getMessage}")
          }
        }
      }
      case _ => println("Please provide a number as argument.")
    }
  }

  private def getCounterTypeFromNumber(number: String): Try[String] = {
    Try {
      val result = counterTypes.filter(_.id == number.toInt)
      result.map(_.name).headOption.getOrElse("No counter type defined.")
    }
  }
}