object either {

  import scala.io.Source
  import java.net.URL

  def getContent(url: URL): Either[String, Source] =
    if (url.getHost.contains("google"))
      Left("Requested URL is blocked for the good of the people!")
    else
      Right(Source.fromURL(url))                  //> getContent: (url: java.net.URL)Either[String,scala.io.Source]

  getContent(new URL("http://danielwestheide.com"))
                                                  //> res0: Either[String,scala.io.Source] = Right(non-empty iterator)
  getContent(new URL("https://plus.google.com"))  //> res1: Either[String,scala.io.Source] = Left(Requested URL is blocked for the
                                                  //|  good of the people!)

  getContent(new URL("http://google.com")) match {
    case Left(msg) => println(msg)
    case Right(source) => source.getLines.foreach(println)
  }                                               //> Requested URL is blocked for the good of the people!

  // Mapping is called on projection (left, right)
  val contentR: Either[String, Iterator[String]] =
    getContent(new URL("http://danielwestheide.com")).right.map(_.getLines())
                                                  //> contentR  : Either[String,Iterator[String]] = Right(non-empty iterator)
  val moreContentR: Either[String, Iterator[String]] =
    getContent(new URL("http://google.com")).right.map(_.getLines)
                                                  //> moreContentR  : Either[String,Iterator[String]] = Left(Requested URL is bloc
                                                  //| ked for the good of the people!)
  val contentL: Either[Iterator[String], Source] =
    getContent(new URL("http://danielwestheide.com")).left.map(Iterator(_))
                                                  //> contentL  : Either[Iterator[String],scala.io.Source] = Right(non-empty itera
                                                  //| tor)
  val moreContentL: Either[Iterator[String], Source] =
    getContent(new URL("http://google.com")).left.map(Iterator(_))
                                                  //> moreContentL  : Either[Iterator[String],scala.io.Source] = Left(non-empty i
                                                  //| terator)

  // Flat mappping (average number of lines of two blog spots)
  // ... simple map
  val part5 = new URL("http://t.co/UR1aalX4")     //> part5  : java.net.URL = http://t.co/UR1aalX4
  val part6 = new URL("http://t.co/6wlKwTmu")     //> part6  : java.net.URL = http://t.co/6wlKwTmu

  val content = getContent(part5).right.map(a =>
    getContent(part6).right.map(b => (a.getLines().size + b.getLines().size) / 2))
                                                  //> content  : Serializable with Product with scala.util.Either[String,Serializ
                                                  //| able with Product with scala.util.Either[String,Int]] = Right(Right(538))
  val contentFM = getContent(part5).right.flatMap(a => getContent(part6).right.map(b =>
    (a.getLines().size + b.getLines().size) / 2)) //> contentFM  : scala.util.Either[String,Int] = Right(538)

  // For comprehension
  def averageLineCount(url1: URL, url2: URL): Either[String, Int] = for {
    source1 <- getContent(url1).right
    source2 <- getContent(url2).right
  } yield (source1.getLines().size + source2.getLines().size) / 2
                                                  //> averageLineCount: (url1: java.net.URL, url2: java.net.URL)Either[String,Int
                                                  //| ]
  averageLineCount(part5, part6)                  //> res2: Either[String,Int] = Right(538)

  // Processing collections.
  type Citizen = String
  case class BlackListedResource(url: URL, visitors: Set[Citizen])
  val blacklist = List(
  	BlackListedResource(new URL("https://google.com"), Set("John Doe", "Johanna Doe")),
  	BlackListedResource(new URL("http://yahoo.com"), Set.empty),
  	BlackListedResource(new URL("https://maps.google.com"), Set("John Doe")),
  	BlackListedResource(new URL("http://plus.google.com"), Set.empty))
                                                  //> blacklist  : List[either.BlackListedResource] = List(BlackListedResource(ht
                                                  //| tps://google.com,Set(John Doe, Johanna Doe)), BlackListedResource(http://ya
                                                  //| hoo.com,Set()), BlackListedResource(https://maps.google.com,Set(John Doe)),
                                                  //|  BlackListedResource(http://plus.google.com,Set()))

  val checkedBlacklist: List[Either[URL, Set[Citizen]]] = blacklist.map(resource =>
    if (resource.visitors.isEmpty) Left(resource.url)
    else Right(resource.visitors))                //> checkedBlacklist  : List[Either[java.net.URL,Set[either.Citizen]]] = List(R
                                                  //| ight(Set(John Doe, Johanna Doe)), Left(http://yahoo.com), Right(Set(John Do
                                                  //| e)), Left(http://plus.google.com))

	val suspiciousResources = checkedBlacklist.flatMap(_.left.toOption)
                                                  //> suspiciousResources  : List[java.net.URL] = List(http://yahoo.com, http://p
                                                  //| lus.google.com)
	val problemCitizens = checkedBlacklist.flatMap(_.right.toOption).flatten.toSet
                                                  //> problemCitizens  : scala.collection.immutable.Set[either.Citizen] = Set(Joh
                                                  //| n Doe, Johanna Doe)
}