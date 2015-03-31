package akka

import akka.actor._
import scala.concurrent.Future
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.Source
import akka.actor._

case class Msg(info: String)

class parent extends Actor {
  def receive = {
    case Msg(info) => sender ! "Message recieved."
  }
}
class child extends Actor {
  val parent = MsgDelRel.system.actorOf(Props[parent], "parent1")
  def receive = {
    case Msg(info) => {
      for (i <- 1 to 5) {
        parent ! Msg("Message " + i + " Sent to parent1")
      }
    }
    case _=>println("Ok,confirmed")
  }
  MsgDelRel.system.shutdown()
}
object MsgDelRel {
  val system = ActorSystem("MyActorSystem")
  val parent = system.actorOf(Props[parent], "parent")
  val child = system.actorOf(Props[child], "child")
  implicit val timeout = Timeout(5.second)
  val M5 = child ! Msg("Trigger")
  for (i <- 1 to 5) {
    val m = parent ? Msg("Message " + i + " Sent to parent")
    val result =Await.result(m, timeout.duration)
    println(result)
  }
}