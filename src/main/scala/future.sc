import neophytes.guide.to.scala.future.Sequential
import neophytes.guide.to.scala.future.Futures
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Success, Failure }

object future {
  Sequential.prepareCappuccino()                  //> res0: scala.util.Try[neophytes.guide.to.scala.future.Sequential.Cappuccino] 
                                                  //| = Success(cappuccino)
  Futures.grind("arabica beans").onComplete {
    case Success(ground) => println(s"got my $ground")
    case Failure(ex) => println("This grinder needs a replacement, seriously!")
  }                                               //> start grinding...

  Thread.sleep(3000)                              //> finished grinding...
                                                  //| got my ground coffee of arabica beans
  val temperatureOkay: Future[Boolean] = Futures.heatWater(Futures.Water(25)).map {
    water =>
      println("we're in the future!")
      (80 to 85).contains(water.temperature)
  }                                               //> heating the water now
                                                  //| temperatureOkay  : scala.concurrent.Future[Boolean] = scala.concurrent.impl.
                                                  //| Promise$DefaultPromise@56d73c7a
}