package akka

import akka.actor._
import scala.concurrent.Future
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.Source
import akka.actor._

case class Word(word: String)
case class File(file: String)
case class Line(line: String)
case class Number(words: Int)

class ParentActor extends Actor {
  implicit val timeout = Timeout(5.second)
  val childActor = context.actorOf(Props[ChildActor], name = "childActor")
  var count = 0
  def receive = {
    case File(file) => {
      for (line <- Source.fromFile(file).getLines()) {
        val future = childActor ? Line(line)
        val result = Await.result(future, timeout.duration).asInstanceOf[Number]
        count = count + result.words
      }
      println(s"Total words in file: $count")
      Main.system.shutdown()
    }

  }
}
class ChildActor extends Actor {
  var words = 0
  def receive = {
    case Line(line: String) => words = line.split(" ").size
      sender ! Number(words)
  }
}

object Main extends App {
  val system = ActorSystem("HelloSystem")
  val parentActor = system.actorOf(Props[ParentActor], name = "parentActor")
  val path = "/home/knoldus/Desktop/data"
  parentActor ! File(path)

}
