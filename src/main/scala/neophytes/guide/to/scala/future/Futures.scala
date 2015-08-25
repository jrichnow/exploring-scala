package neophytes.guide.to.scala.future

import scala.concurrent.future
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Random
import scala.util.Try

object Futures {

  type CoffeeBeans = String
  type GroundCoffee = String
  case class Water(temperature: Int)
  type Milk = String
  type FrothedMilk = String
  type Espresso = String
  type Cappuccino = String

  case class GrindingException(msg: String) extends Exception(msg)
  case class FrothingException(msg: String) extends Exception(msg)
  case class WaterBoilingException(msg: String) extends Exception(msg)
  case class BrewingException(msg: String) extends Exception(msg)

  def grind(beans: CoffeeBeans): Future[GroundCoffee] = future {
    println("start grinding...")
    Thread.sleep(Random.nextInt(2000))
    if (beans == "baked beans") throw GrindingException("are you joking?")
    println("finished grinding...")
    s"ground coffee of $beans"
  }

  def heatWater(water: Water): Future[Water] = future {
    println("heating the water now")
    Thread.sleep(Random.nextInt(2000))
    println("hot, it's hot!")
    water.copy(temperature = 85)
  }

  def frothMilk(milk: Milk): Future[FrothedMilk] = future {
    println("milk frothing system engaged!")
    Thread.sleep(Random.nextInt(2000))
    println("shutting down milk frothing system")
    s"frothed $milk"
  }

  def brew(coffee: GroundCoffee, heatedWater: Water): Future[Espresso] = future {
    println("happy brewing :)")
    Thread.sleep(Random.nextInt(2000))
    println("it's brewed!")
    "espresso"
  }
  
  def combine(espresso: Espresso, frothedMilk: FrothedMilk): Future[Cappuccino] = future {
    println("combining :)")
    Thread.sleep(Random.nextInt(2000))
    println("it's done!")
    "cappuccino"}

//  def prepareCappuccino(): Future[Cappuccino] = {
//    val groundCoffee = grind("arabica beans")
//    val heatedWater = heatWater(Water(20))
//    val frothedMilk = frothMilk("milk")
//    for {
//      ground <- groundCoffee
//      water <- heatedWater
//      foam <- frothedMilk
//      espresso <- brew(ground, water)
//    } yield combine(espresso, foam)
//  }
}