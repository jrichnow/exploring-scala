object forcomprehension {

  def gameResults(): Seq[(String, Int)] = ("Daniel", 3500) :: ("Melissa", 13000) :: ("John", 7000) :: Nil
                                                  //> gameResults: ()Seq[(String, Int)]
	def hallOfFame = for {
		(name, score) <- gameResults
		if (score > 5000)
	} yield name                              //> hallOfFame: => Seq[String]
	
	println(hallOfFame)                       //> List(Melissa, John)
}