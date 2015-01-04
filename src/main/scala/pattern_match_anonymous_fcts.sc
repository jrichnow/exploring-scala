object pattern_match_anonymous_fcts {

  val wordFrequencies = List(
    ("habitual", 6),
    ("and", 56),
    ("consuetudinary", 2),
    ("additionally", 27),
    ("homely", 5),
    ("society", 13))                              //> wordFrequencies  : List[(String, Int)] = List((habitual,6), (and,56), (consu
                                                  //| etudinary,2), (additionally,27), (homely,5), (society,13))

  def wordsWithOutliers(wordFrequencies: Seq[(String, Int)]): Seq[String] =
    wordFrequencies.filter(wf => wf._2 > 3 && wf._2 < 25).map(_._1)
                                                  //> wordsWithOutliers: (wordFrequencies: Seq[(String, Int)])Seq[String]

  println(wordsWithOutliers(wordFrequencies))     //> List(habitual, homely, society)

	// Using pattern matching anonymous function
  def wordsWithOutliers1(wordFrequencies: Seq[(String, Int)]): Seq[String] =
    wordFrequencies.filter {
      case (_, f) =>
        f > 3 && f < 25
    } map { case (w, _) => w }                    //> wordsWithOutliers1: (wordFrequencies: Seq[(String, Int)])Seq[String]
    println(wordsWithOutliers1(wordFrequencies))  //> List(habitual, homely, society)
}