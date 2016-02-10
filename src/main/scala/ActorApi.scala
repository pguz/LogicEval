import actors.{WorkerProtocol, WorkerActor}
import actors.WorkerProtocol.ResExpr
import akka.actor.{PoisonPill, Props, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import logic.Expr
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

trait ActorApi {
  val system =
    ActorSystem("Evaluator", akkaConfig)
  implicit val timeout: Timeout =
    Timeout(30, SECONDS)


  def akkaConfig = ConfigFactory.parseString(s"""
    akka {
      loglevel = DEBUG
    }
  """)

  def processExprWithActors(expr: Expr): Future[Boolean] = {
    val leaderActor =
      system.actorOf(Props(classOf[WorkerActor]), "Leader")
    val f = leaderActor ? WorkerProtocol.EvalExpr(expr)
    f.mapTo[ResExpr].map {
      case ResExpr(result) => leaderActor ! PoisonPill; result
    }

  }
}
