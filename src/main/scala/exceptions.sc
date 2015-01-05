object exceptions {
	
	// The Java way.
  case class Customer(age: Int)
  class Cigarettes
  case class UnderAgeException(message: String) extends Exception(message)
  
  def buyCigarettes(customer: Customer): Cigarettes =
    if (customer.age < 16) throw UnderAgeException(s"Customer must be older than 16 but was ${customer.age}")
    else new Cigarettes                           //> buyCigarettes: (customer: exceptions.Customer)exceptions.Cigarettes

	val youngCustomer = Customer(15)          //> youngCustomer  : exceptions.Customer = Customer(15)
	
	try {
		buyCigarettes(youngCustomer)
	} catch {
		case UnderAgeException(msg) => msg
	}                                         //> res0: Object = Customer must be older than 16 but was 15
	
	// The Scala way.
	// ---------------------
	import scala.util.Try
  import java.net.URL

	def parseURL(url:String):Try[URL] = Try(new URL(url))
                                                  //> parseURL: (url: String)scala.util.Try[java.net.URL]
	parseURL("http://danielwestheide.com")    //> res1: scala.util.Try[java.net.URL] = Success(http://danielwestheide.com)
	parseURL("garbage")                       //> res2: scala.util.Try[java.net.URL] = Failure(java.net.MalformedURLException:
                                                  //|  no protocol: garbage)
	parseURL("garbage").getOrElse(new URL("http://duckduckgo.com"))
                                                  //> res3: java.net.URL = http://duckduckgo.com
	// (flat)mapping
	parseURL("http://danielwestheide.com").map(_.getProtocol)
                                                  //> res4: scala.util.Try[String] = Success(http)
	parseURL("garbage").map(_.getProtocol)    //> res5: scala.util.Try[String] = Failure(java.net.MalformedURLException: no pr
                                                  //| otocol: garbage)
	// filter and foreach
	def parseHttpURL(url:String) = parseURL(url).filter(_.getProtocol == "http")
                                                  //> parseHttpURL: (url: String)scala.util.Try[java.net.URL]
  parseHttpURL("http://apache.openmirror.de")     //> res6: scala.util.Try[java.net.URL] = Success(http://apache.openmirror.de)
  parseHttpURL("ftp://mirror.netcologne.de/apache.org")
                                                  //> res7: scala.util.Try[java.net.URL] = Failure(java.util.NoSuchElementExcepti
                                                  //| on: Predicate does not hold for ftp://mirror.netcologne.de/apache.org)
  parseHttpURL("http://danielwestheide.com").foreach(println)
                                                  //> http://danielwestheide.com
	
	// for comprehension (if any Try fails evaluation stops)
	import scala.io.Source
	def getURLContent(url:String): Try[Iterator[String]] =
		for {
			url <- parseURL(url)
			connection <- Try(url.openConnection())
			is <- Try(connection.getInputStream)
			source = Source.fromInputStream(is)
	} yield source.getLines()                 //> getURLContent: (url: String)scala.util.Try[Iterator[String]]

	// Pattern matching
	import scala.util.Success
	import scala.util.Failure
	
	getURLContent("http://www.framedobjects.com") match {
		case Success(lines) => lines.foreach(println)
		case Failure(ex) => println(s"Problem rendering URL content: ${ex.getMessage}")
	}                                         //> <!DOCTYPE html>
                                                  //| <html lang="en">
                                                  //| 
                                                  //| <head>
                                                  //| 
                                                  //| <meta charset="utf-8">
                                                  //| <meta http-equiv="X-UA-Compatible" content="IE=edge">
                                                  //| <meta name="viewport" content="width=device-width, initial-scale=1">
                                                  //| <meta name="description" content="">
                                                  //| <meta name="author" content="">
                                                  //| 
                                                  //| <title>Framed Objects &mdash; Modern Software Development</title>
                                                  //| 
                                                  //| <!-- Bootstrap Core CSS -->
                                                  //| <link href="css/bootstrap.min.css" rel="stylesheet">
                                                  //| 
                                                  //| <!-- Custom CSS -->
                                                  //| <link href="css/stylish-portfolio.css" rel="stylesheet">
                                                  //| 	
                                                  //| <!-- Custom Fonts -->
                                                  //| <link href="font-awesome-4.2.0/css/font-awesome.min.css" rel="stylesheet" t
                                                  //| ype="text/css">
                                                  //| <link href="http://fonts.googleapis.com/css?family=Source+Sans+Pro:300,400,
                                                  //| 700,300italic,400italic,700italic"
                                                  //| 	rel="stylesheet" type="text/css">
                                                  //| 
                                                  //| <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media quer
                                                  //| ies -->
                                                  //| <!-- WARNING: Respond.js
                                                  //| Output exceeds cutoff limit.
  // Recover from a Failure
  import java.net.MalformedURLException
  import java.io.FileNotFoundException
  
  val content = getURLContent("garbage") recover {
  	case e: FileNotFoundException => Iterator("Requested page does not exist")
  	case e: MalformedURLException => Iterator("Please make sure to enter a valid URL")
  	case _ => Iterator("An unexpected error has occurred. We are so sorry!")
  }                                               //> content  : scala.util.Try[Iterator[String]] = Success(non-empty iterator)
  
  content.get.foreach(println)                    //> Please make sure to enter a valid URL
}