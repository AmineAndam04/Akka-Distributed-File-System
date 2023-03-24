
package dbmng
import scala.collection.mutable.{HashMap, MultiMap, Set}
import akka.actor.{ActorSystem, Props, ActorRef, Actor}
import scala.collection.mutable.Map
import java.io.FileWriter
import java.io.IOException
import scala.io.Source

class InmMemoryKVStore extends Actor{
  import Messages._
  var vkstore = readthefile()
  
  override def receive : Receive ={
    case Addtofile(key,value,delorad) => addtofile(key,value,delorad)
    case InMemLookup(key) => inMemLookup(key)
    case Stop => context.stop(self)
    
  }
  def addtofile(key : String, value : String,delorad : Int): Unit= {
    //Thread.sleep(8000)
    val fw = new FileWriter("src/files/Store.txt",true)
    def newMethod(): Unit =
      fw.write(key + ":" + value + "\n")

    newMethod()
    fw.close()
    // 1 to add, 0 to delete
    if (delorad == 1){
        vkstore += (key->value)
        println("The value is added")
    } else{
      if (vkstore.contains(key)) {
        vkstore -= key
        println("The value is deleted")
      } else {
        println("The key is not stored to be deleted.")
        }
        
    }
    
  }
  def inMemLookup(key: String): Unit={
  //Thread.sleep(8000)
    vkstore.get(key) match {
        case Some(value) => 
            if( value == "deleted"){
                println("The key doesn't exist (deleted)")
            }else {
                println(s"The key does exist in our dataset with a value = $value")
            }
        case None => println("The key  does not exist")
        }
  }

  def readthefile() : HashMap[String, String]= {
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
    val hm = pairs.toMap
    val myHashMap = collection.mutable.HashMap(hm.toSeq: _*) // convert to HashMap
    return myHashMap

  }
}



