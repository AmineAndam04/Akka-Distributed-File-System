object CreateDataStore extends App {
  import scala.collection.mutable.HashMap
  import java.io.FileWriter
  import scala.collection.mutable.HashMap
  import scala.io.Source

  // intial data
  val hashMap = HashMap("key1" -> "value1", "key2" -> "value2", "key3" -> "value3","key4" -> "value4")

  // Créer un FileWriter pour écrire dans un fichier nommé "hashMap.txt"
  val fw = new FileWriter("src/files/Store.txt")

  // Insert the elements
  for ((key, value) <- hashMap) {
    fw.write(key + ":" + value + "\n")
  }
  fw.close()
  //Reaad the text file
  val source = Source.fromFile("src/files/Store.txt")
  val lines = source.getLines.toList
  source.close()
  // List to store values
  var pairs = List[(String, String)]()
  for (line <- lines) {
    val split = line.split(":")
    val key = split(0)
    val value = split(1)
    pairs = pairs :+ (key, value)
  }
  import scala.collection.mutable.Map
  val hm = pairs.toMap
  val myHashMap = collection.mutable.HashMap(hm.toSeq: _*) // convert to HashMap

  println(myHashMap) // print the resulting HashMap
}
