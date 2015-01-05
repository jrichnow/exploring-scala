import neophytes.guide.to.scala.option.User
import neophytes.guide.to.scala.option.UserRepository

object option {

  val user = User(2, "Johanna", "Doe", 30, None)  //> user  : neophytes.guide.to.scala.option.User = User(2,Johanna,Doe,30,None)

  val gender = user.gender match {
    case Some(gender) => gender
    case None => "not specified"
  }                                               //> gender  : String = not specified

  UserRepository.findById(2).foreach(u => println(u.firstName))
                                                  //> Johanna
  UserRepository.findById(1).map(_.age)           //> res0: Option[Int] = Some(32)
  UserRepository.findById(1).map(_.gender)        //> res1: Option[Option[String]] = Some(Some(male))
  UserRepository.findById(1).flatMap(_.gender)    //> res2: Option[String] = Some(male)
  UserRepository.findById(2).flatMap(_.gender)    //> res3: Option[String] = None

  val names: List[List[String]] =
    List(
      List("John", "Johanna", "Daniel"),
      List(),
      List("Doe", "Westheide"))                   //> names  : List[List[String]] = List(List(John, Johanna, Daniel), List(), List
                                                  //| (Doe, Westheide))

  names.map(_.map(_.toUpperCase))                 //> res4: List[List[String]] = List(List(JOHN, JOHANNA, DANIEL), List(), List(DO
                                                  //| E, WESTHEIDE))
  names.flatMap(xs => xs.map(_.toUpperCase()))    //> res5: List[String] = List(JOHN, JOHANNA, DANIEL, DOE, WESTHEIDE)

  // Using list with Option
  val names1: List[Option[String]] =
    List(Some("Johanna"), None, Some("Daniel"))   //> names1  : List[Option[String]] = List(Some(Johanna), None, Some(Daniel))

  names1.map(_.map(_.toUpperCase))                //> res6: List[Option[String]] = List(Some(JOHANNA), None, Some(DANIEL))
  names1.flatMap(xs => xs.map(_.toUpperCase))     //> res7: List[String] = List(JOHANNA, DANIEL)

  // Using filter with Option
  UserRepository.findById(1).filter(_.age > 30)   //> res8: Option[neophytes.guide.to.scala.option.User] = Some(User(1,John,Doe,3
                                                  //| 2,Some(male)))
  UserRepository.findById(2).filter(_.age > 30)   //> res9: Option[neophytes.guide.to.scala.option.User] = None
  UserRepository.findById(3).filter(_.age > 30)   //> res10: Option[neophytes.guide.to.scala.option.User] = None

  // Using for comprehensions
  for {
    user <- UserRepository.findById(1)
    gender <- user.gender
  } yield gender                                  //> res11: Option[String] = Some(male)

  for {
    user <- UserRepository.findAll
    gender <- user.gender
  } yield gender                                  //> res12: Iterable[String] = List(male)

  for {
    User(_, _, _, _, Some(gender)) <- UserRepository.findAll
  } yield gender                                  //> res13: Iterable[String] = List(male)
}