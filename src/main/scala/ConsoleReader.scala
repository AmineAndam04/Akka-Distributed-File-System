package dbmng
import scala.io.StdIn.readLine
import scala.util.control.Breaks._
import akka.actor.{ActorSystem, Props, ActorRef, Actor}
import scala.io.Source

class ConsoleReader(targetActor: ActorRef) extends Actor {
  import Messages._
   override def preStart() = {
    println("You are logged in. Type and press ret. 'DISCONNECT' to log out.")
    self ! "read"
  }
  def receive: Receive = {
    case "read" =>
      var a = 0
      while ( a<5 ) {
        Thread.sleep(3000)
        println("What do you want : Store, Lookup or Delete")
        val input = readLine("prompt> ")
        input match {
          case "Store" | "store"  => 
            println("Give me the key")
            var key = readLine()
            println("Give me the value")
            var value = readLine()
            targetActor ! Store( key , value )

          case "Lookup" | "lookup"  => 
            println("Give me the key")
            var key = readLine()
            targetActor ! Lookup(key)

          case "Delete" | "delete"  => 
            println("Give me the key")
            var key = readLine()
            targetActor ! Delete(key)
          case _  => 
            targetActor ! Stop
            context.stop(self)
            println("Terminated")
            a=10

      }
    }

  }

}
