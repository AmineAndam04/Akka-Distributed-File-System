
package dbmng
import akka.cluster.Cluster
import scala.io.StdIn.readLine
import java.io._
import scala.util.Random
import scala.concurrent.duration._
//Implement the Key-value store on an independent actor
//Implement the console reader on an independent actor
import akka.actor.{ActorSystem, Props, ActorRef, Actor}
//import akka.actor.SupervisorStrategy.{Escalate, Stop, Resume, Restart}
import scala.collection.mutable.HashMap
import scala.compiletime.ops.string
//import dbmng.VKStore.vkstore
import sun.misc.Signal
import sun.misc.SignalHandler
import dbmng.Messages.Store
//import dbmng.Messages.Addtofile.value
import akka.util.Timeout
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Success
import scala.concurrent.ExecutionContext

object Messages {
  case class Store( key : String, value : String )
  case class Lookup( key : String )
  case class Delete( key : String )
  case class Addtofile(key : String, value : String,delorad : Int)
  case class InMemLookup(key : String)
  case class CHLookup(key: String)
  case class CHStore(key: String, value: String)
  case class CHDelete(key: String)
  case class Data(key: String, value: String)
  case class Stored(key: String, value : String)
  case class Deleted(key: String)
  case object Stop
}
/*class VKStore extends Actor {
  import Messages._
  var vkstore = HashMap("C"->"Csharp", "S"->"Scala", "J"->"Java")
  
  override def receive : Receive ={
    case Store( key , value ) =>store(key , value )
    case Lookup(key) =>lookup(key)
    case Delete(key) => delete(key)

  }
  
  
   def store(key : String, value : String): Unit= {
    vkstore += (key->value)
    println("Value added")
    println(vkstore)
  }
  def lookup( key : String ): Unit = {
      val look = vkstore.contains(key) 
      println(look)
   }
   def delete( key : String ): Unit = {
      vkstore -= key
   }
}*/

/*class ConsoleReader(targetActor: ActorRef) extends Actor {
  import Messages._
  def receive: Receive = {
    case "read" =>
      var a = 0
      while ( a<5 ) {
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

}*/
object CHAkkaSystemMain extends App {
  val as = ActorSystem("ActorSystem")
  val OurSource = as.actorOf(Props[SourceActor](),"OurSource")
  val CordActor = as.actorOf(Props(new CoordinatorActor(OurSource,3)),"Coordinator") // The coordinator
  val cachActor = as.actorOf(Props(new CacheActor(CordActor)))
  val mystore = as.actorOf(Props(new CHVKStore(cachActor)),"Mystore")
  val Cnsl = as.actorOf(Props(new ConsoleReader(mystore)), "MyConsole")
  Cnsl ! "read"
}

object AkkaSystemMain extends App {
  val as = ActorSystem("ActorSystem")
  val InMem = as.actorOf(Props[InmMemoryKVStore](),"InMemoryStore")
  val mystore = as.actorOf(Props(new VKStore(InMem)),"Mystore")
  val Cnsl = as.actorOf(Props(new ConsoleReader(mystore)), "MyConsole")
  Cnsl ! "read"
}


object RandomMain extends  App {
  import Messages._
  // populate the file
  /*import java.io.{PrintStream, FileOutputStream,PrintWriter}
  val file = new File("src/files/output.txt")
  val out = new PrintStream(new FileOutputStream(file))
  System.setOut(out)
  println("This print will be redirected to the file")
 if (true) {
  println("This print will be redirected to the file")
}*/

  // Redirect output to the file
  import java.io.{File, FileOutputStream, PrintStream}
  val file = new File("src/files/output.txt")
    val fos = new FileOutputStream(file)
    val ps = new PrintStream(fos)
     val originalOut = System.out
    System.setOut(ps)
  def PopulateFile( ): Unit = {
      var key_index = 1
      val filename = "src/files/Store.txt"
      val file = new File(filename)
      if (file.exists) {
          file.delete
      }
      val fw = new FileWriter(filename,true)
      def newMethod(key : String, value : String): Unit =
          fw.write(key + ":" + value + "\n")
      for (i <- 1 to 500) {
        newMethod("key" + key_index, "value" + key_index)
        key_index += 1
      }
      fw.close()
   }
  PopulateFile( )
  val startTime1 = System.nanoTime()
  var key_index = 501
  // without cash:
  val as = ActorSystem("ActorSystem")
  val InMem = as.actorOf(Props[InmMemoryKVStore](),"InMemoryStore")
  var mystore = as.actorOf(Props(new VKStore(InMem)),"Mystore")
  
  var lastlookedup = 0
  // Randomly choose an action
  for (simulation <- 1 to 500){
    println(simulation)
    val randomValue = Random.nextInt(3)
    randomValue match {
    case 0 => 
      mystore ! Store("key" + key_index, "value" + key_index)
      key_index += 1
    case 1 => 
      // choose randomly a key to delete
      val randomValue = Random.nextInt(key_index)
      mystore ! Delete("key" + randomValue)
    case 2 => 
      if (lastlookedup!= 0) {
        val rand = new Random()
        val randomBoolean = rand.nextBoolean()
        if (randomBoolean) {
          mystore ! Lookup("key" + lastlookedup)
        } else {
        // create the bias
          val rand = new Random()
          val randomDouble = rand.nextDouble()
          val threshold = 0.3
          if (randomDouble < threshold) {
            val a = 501
            val b = key_index
            val randomKey = a + Random.nextInt((b - a))
            mystore ! Lookup("key" + randomKey)
            lastlookedup = randomKey
          }else {
            val a = 1
            val b = 500
            val randomKey = a + Random.nextInt((b - a)+1)
            mystore ! Lookup("key" + randomKey)
            lastlookedup = randomKey
         }
      }
    }else {
      val a = 1
      val b = 500
      val randomKey = a + Random.nextInt((b - a)+1)
      mystore ! Lookup("key" + randomKey)
      lastlookedup = randomKey

    }
  }
}
  val endTime1 = System.nanoTime()
  val elapsedTime1 = (endTime1 - startTime1)/ 1000000000.0
  

  PopulateFile( )
  val startTime2 = System.nanoTime()
  key_index = 501
  // withcash:
  val asc = ActorSystem("ActorSystem")
  val OurSource = asc.actorOf(Props[SourceActor](),"OurSource")
  val CordActor = asc.actorOf(Props(new CoordinatorActor(OurSource,50)),"Coordinator") // The coordinator
  val cachActor = asc.actorOf(Props(new CacheActor(CordActor)))
  mystore = asc.actorOf(Props(new CHVKStore(cachActor)),"Mystore")
  lastlookedup = 0
  for (simulation <- 1 to 500){
    println(simulation)
    val randomValue = Random.nextInt(3)
    randomValue match {
    case 0 => 
      mystore ! Store("key" + key_index, "value" + key_index)
      key_index += 1
    case 1 => 
      // choose randomly a key to delete
      val randomValue = Random.nextInt(key_index)
      mystore ! Delete("key" + randomValue)
    case 2 => 
      if (lastlookedup!= 0) {
        val rand = new Random()
        val randomBoolean = rand.nextBoolean()
        if (randomBoolean) {
          mystore ! Lookup("key" + lastlookedup)
        } else {
        // create the bias
          val rand = new Random()
          val randomDouble = rand.nextDouble()
          val threshold = 0.3
          if (randomDouble < threshold) {
            val a = 501
            val b = key_index
            val randomKey = a + Random.nextInt((b - a))
            mystore ! Lookup("key" + randomKey)
            lastlookedup = randomKey
          }else {
            val a = 1
            val b = 500
            val randomKey = a + Random.nextInt((b - a)+1)
            mystore ! Lookup("key" + randomKey)
            lastlookedup = randomKey
         }
      }
    }else {
      val a = 1
      val b = 500
      val randomKey = a + Random.nextInt((b - a)+1)
      mystore ! Lookup("key" + randomKey)
      lastlookedup = randomKey

    }
  }
}
  System.setOut(originalOut)
  val endTime2 = System.nanoTime()
  val elapsedTime2 = (endTime2 - startTime2)/ 1000000000.0
  System.out.println(s"Without cache it takes $elapsedTime1 seconds")
  System.out.println(s"Without cache it takes $elapsedTime2 seconds")
 
}

object ClInmMemoryKVStore extends App {
  val as = ActorSystem("ClusterSystem")
  val cluster = Cluster(as)
  cluster.join(cluster.selfAddress)
  val InMem = as.actorOf(Props[InmMemoryKVStore](),"InMemoryStore")
  
}
object  ClVKStore extends  App {
  val as = ActorSystem("ClusterSystem")
  val cluster = Cluster(as)
  cluster.join(cluster.selfAddress)

  val InMem = as.actorOf(Props[InmMemoryKVStore](),"InMemoryStore")
  val mystore = as.actorOf(Props(new VKStore(InMem)),"Mystore")
  
}
object  ClCnsl  extends  App {
  val as = ActorSystem("ClusterSystem")
  val cluster = Cluster(as)
  cluster.join(cluster.selfAddress)
  val InMem = as.actorOf(Props[InmMemoryKVStore](),"InMemoryStore")
  val mystore = as.actorOf(Props(new VKStore(InMem)),"Mystore")
  val Cnsl = as.actorOf(Props(new ConsoleReader(mystore)), "MyConsole")
  Cnsl ! "read"
  /*println("You are here")
  val KVStoreAddress = "akka.tcp://ClusterSystem@127.0.0.1:2553/user/Mystore"
  /*as.actorSelection(KVStoreAddress).resolveOne(3 seconds).onSuccess {
    case mystore : ActorRef => {
      as.actorOf(Props(new ConsoleReader(mystore)), "MyConsole")
    }
  }*/
  println("You are here")
  implicit val timeout: Timeout = Timeout(3.seconds)
  val actorRefFuture = as.actorSelection(KVStoreAddress).resolveOne()
  println("You are here")
  actorRefFuture.onComplete {
  case scala.util.Success(actorRef) =>
    println("You are here")
    val cnsl = as.actorOf(Props(new ConsoleReader(actorRef)), "MyConsole") 
    cnsl ! "read"
     
  case scala.util.Failure(ex) =>
    // handle the failure of the future, passing in the resulting exception
}*/

}