package akka

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.io.Source
import scala.util.Failure
import scala.util.Success
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

case class Word(word: String)
case class File(file: String)
case class Line(line: String)
case class Number(words: Int)
case object Count

class ParentActor extends Actor {
  implicit val timeout = Timeout(5.second)
  val childActor = context.actorOf(Props[ChildActor], name = "childActor")
  var count = 0
  def receive = {
    case File(file) => {
      for (line <- Source.fromFile(file).getLines()) {
        val future = childActor ? Line(line)
        future onComplete {
          case Success(res) => {
           count= count + res.asInstanceOf[Number].words
           println("I "+count)
          } 
          case Failure(f)   => println("something wrong happened")
        }
      }
    }
    case Count=> {
      println("count------------",count)
      sender ! count
    }
  }
}
class ChildActor extends Actor {
  def receive = {
    case Line(line: String) =>
      sender ! Number(line.split(" ").size)
  }
}

object Main extends App {
  val system = ActorSystem("HelloSystem")
  val parentActor = system.actorOf(Props[ParentActor], name = "parentActor")
  implicit val timeout = Timeout(5.second)
  parentActor ! File("data")
  val result = parentActor ? Count
  result map{res=>
    println("final result ---------------",res)
  }
  
  //Main.system.shutdown()
}
