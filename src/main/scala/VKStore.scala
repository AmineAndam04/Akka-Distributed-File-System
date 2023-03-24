package dbmng
import scala.io.StdIn.readLine
import scala.util.control.Breaks._
import akka.actor.{ActorSystem, Props, ActorRef, Actor}
import scala.collection.mutable.HashMap
import java.io.FileWriter
import java.io.IOException
import scala.io.Source
class VKStore(targetInMemActor: ActorRef) extends Actor {
  import Messages._
  override def receive : Receive ={
    case Store( key , value ) =>  targetInMemActor !   Addtofile(key,value,1)
    case Lookup(key) => targetInMemActor ! InMemLookup(key)
    case Delete(key) => targetInMemActor ! Addtofile(key,"deleted",0)
    case Stop => context.stop(self)
  }
  // These are no longer used. The InMenVKStore is doing the job
  def store(key : String, value : String): Unit= {
    appendend(key,value)
  }
  def lookup( key : String ): Unit = {
    val source = Source.fromFile("src/files/Store.txt")
    val lines = source.getLines.toSeq.reverse
    source.close()
    // List to store values
    var pairs = List[(String, String)]()
    var founded = 0
    breakable {for (line <- lines) {
        val split = line.split(":")
        val keys = split(0)
        if (keys == key) {
            founded = 1
            val value = split(1)
            if (value == "deleted") {
                println("The key is not here (it was deleted)")
                break
                
            } else {
                println("The key is in the database")
                break
            }
        }  
    }}
    if (founded == 0) {
        println("The key is not here")
    }
    println(" What do you want : Store, Lookup or Delete")
   }
   def delete( key : String ): Unit = {
      appendend(key,"deleted")
   }
   def appendend(key :String, value : String): Unit= {
    val fw = new FileWriter("src/files/Store.txt",true)
    def newMethod(): Unit =
      fw.write(key + ":" + value + "\n")

    newMethod()
    fw.close()
   }
}

class CHVKStore(targetInMemActor: ActorRef) extends Actor {
  import Messages._
  override def receive : Receive ={
    case Store( key , value ) =>  targetInMemActor !  CHStore(key, value) 
    case Lookup(key) => targetInMemActor !  CHLookup(key) 
    case Delete(key) => targetInMemActor ! CHDelete(key) 
    case Stop => context.stop(self)
  }
  
}