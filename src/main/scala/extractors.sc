import neophytes.guide.to.scala.extractors.FreeUser
import neophytes.guide.to.scala.extractors.PremiumUser
import neophytes.guide.to.scala.extractors.premiumCandidate
import neophytes.guide.to.scala.extractors.GivenNames

object extractors {
  val user = new FreeUser("Daniel", 3000, 0.76d)  //> user  : neophytes.guide.to.scala.extractors.FreeUser = neophytes.guide.to.sc
                                                  //| ala.extractors.FreeUser@2107ebe1
  user match {
    case FreeUser(name, _, p) => {
      if (p > 0.75) s"$name what I can I do for you"
      else s"Hello $name"
    }
    case PremiumUser(name, _) => s"Welcome back $name"
  }                                               //> res0: String = Daniel what I can I do for you
  
  user match {
  	case freeUser @ premiumCandidate() => s"spam him"
  	case _ => s"regular letter"
  }                                               //> res1: String = spam him
  
  // infix operation pattern
  val xs = 58 #:: 43 #:: 93 #:: Stream.empty      //> xs  : scala.collection.immutable.Stream[Int] = Stream(58, ?)
  xs match {
   case first #:: second #:: _ => first - second
   case _ => -1
  }                                               //> res2: Int = 15
  
  // matching by list size
  val xs1 = 3 :: 6 :: 12 :: Nil                   //> xs1  : List[Int] = List(3, 6, 12)
  xs1 match {
  	case List(a, b) => a * b
  	case List(a, b, c) => a + b + c
  	case _ => 0
  }                                               //> res3: Int = 21
  
  // don't care about list size using wildcard operator
  val xs2 = 3 :: 6 :: 12 :: 24 ::  Nil            //> xs2  : List[Int] = List(3, 6, 12, 24)
  xs2 match {
  	case List(a, b, _*) => a * b
  	case _ => 0
  }                                               //> res4: Int = 18
  
  def greetWithFirstName(name:String) = name match {
		case GivenNames(firstName, _*) => s"Good Morning, $firstName"
  	case _ => "Welcome! Please make sure a name is given!"
	}                                         //> greetWithFirstName: (name: String)String

	greetWithFirstName("Tom Jones")           //> res5: String = Good Morning, Tom
}