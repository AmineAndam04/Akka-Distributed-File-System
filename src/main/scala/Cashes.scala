package dbmng
import akka.actor.{ActorSystem, Props}
import scala.io.Source
import scala.collection.mutable.{HashMap, MultiMap, Set}
import scala.collection.mutable.LinkedHashMap

import akka.actor.{ActorSystem, Props, ActorRef, Actor}
import scala.collection.mutable.Map
import java.io.FileWriter
import java.io.IOException
import scala.compiletime.ops.string
// Define messages

// Define cache actor
class CacheActor(coordinator: ActorRef) extends Actor {
  import Messages._
  def receive = {
    case CHLookup(key) =>
      // Ask coordinator for data
      coordinator ! CHLookup(key)
    case CHStore(key, value) =>
      // Tell coordinator to update data
      coordinator ! CHStore(key, value)
    case CHDelete(key) =>
        // Tell coordinator to delete data
      coordinator ! CHDelete(key)
    case Stored(key,value)=>
        println(s"the pair $key and $value are stored")
    case Deleted(key)=>
        println(s"The key : $key is deleted")
    case Data(key, value) =>
      // Do something with received data
      if (key != "NaN" && value != "NaN" ){
            println("The value does exist")
        } else {
            print("The value does not exist")
        }
  }
}

// Define coordinator actor
class CoordinatorActor(source: ActorRef,capacity: Int) extends Actor {
  // Initialize an empty map to store data
  import Messages._
  //var cache = Map.empty[String, String]
  var cache: LinkedHashMap[String, String] = LinkedHashMap()
       
  def receive = {
    case CHLookup(key) =>
      println("The value stored in our cashe before your request are :")
      val pairsString = cache.map { case (key, value) => s"($key)"}.mkString(", ")
      println(pairsString)
      // Check if cache contains key
      if (cache.contains(key)) {
        // Return data from cache
        var vals = cache.get(key).toString 
        cache.remove(key)
        cache += (key -> vals )
        sender() ! Data(key, cache(key))
      } else {
        // Ask source for data and remember original requester
        source ! (CHLookup(key), sender())
      }

    case CHStore(key, value) =>
      // Update cache with new data
      source ! (CHStore(key,value) ,sender())
    case CHDelete(key) =>
      source ! (CHDelete(key) ,sender())
    case (Stored(key, value), requester: ActorRef) =>
        //cache += (key -> value)
        cache.put(key, value)
        requester ! Stored(key, value)
    case (Deleted(key), requester: ActorRef) =>
        cache -= key
        requester ! Deleted(key)
    case (Data(key, value), requester: ActorRef) =>
        if (key != "NaN" && value != "NaN" ){
            //cache += (key -> value)
            if (cache.size > capacity) {
                cache.remove(cache.head._1)
                }
            cache.put(key, value)
            // Forward data to original requester
            println("The value does exist")
            //requester ! Data(key, value)
        } else {
            println("The value does not exist")
            //requester ! Data(key, value)
        }
      
      
  }
}

// Define source actor that reads from a text file
class SourceActor extends Actor {
   import Messages._
   var vkstore = readthefile()
   def receive = {
    case (CHLookup(key), requester: ActorRef) =>
        vkstore.get(key) match {
        case Some(value) =>
            if( value == "deleted"){
                //println("Key deleted")
                sender() ! (Data("NaN", "NaN"), requester)
            }else {
                sender() ! (Data(key, value), requester)
            }
        case None => sender() ! (Data("NaN", "NaN"), requester) 
        } 
    case (CHStore(key,value), requester: ActorRef) => 
        addtofile(key,value,1)
        vkstore += (key -> value)
        sender() ! (Stored(key, value), requester)
    case (CHDelete(key), requester: ActorRef) => 
        addtofile(key,"deleted",0)
        vkstore -= key
        sender() ! (Deleted(key), requester)
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
        vkstore -= key
        println("The value was deleted")
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
}}


// TODO : verify if the structure of the LinkedHashMap is respected