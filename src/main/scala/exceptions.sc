object exceptions {

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
}