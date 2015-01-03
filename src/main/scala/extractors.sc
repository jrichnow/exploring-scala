import neophytes.guide.to.scala.extractors.FreeUser
import neophytes.guide.to.scala.extractors.PremiumUser
import neophytes.guide.to.scala.extractors.premiumCandidate

object extractors {
  val user = new FreeUser("Daniel", 3000, 0.76d)  //> user  : neophytes.guide.to.scala.extractors.FreeUser = neophytes.guide.to.sc
                                                  //| ala.extractors.FreeUser@31f26605
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
}