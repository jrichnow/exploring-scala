package utils
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io._
import java.net.URL

object SendRequests {

  def main(args: Array[String]) {
    import scala.concurrent.ExecutionContext.Implicits.global
    val currentTime = System.currentTimeMillis()
    
    val url = s"http://localctl.test:8080/adserver-ih/tpui/400381438042202155/${currentTime-100}/22?tpid=999&tpuid=10000088"
    Source.fromURL(new URL(url))
    println("done")
  }
}