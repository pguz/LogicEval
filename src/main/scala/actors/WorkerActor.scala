package actors

import actors.WorkerProtocol.{EvalExpr, ResExpr}
import akka.actor.{PoisonPill, Actor, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import logic._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Success

object WorkerProtocol {

  sealed trait WorkerRequest
  case class EvalExpr(expr: Expr)
    extends WorkerRequest

  sealed trait WorkerResponse
  case class ResExpr(result: Boolean)
    extends WorkerResponse
}


class WorkerActor extends Actor {
  implicit val timeout: Timeout =
    Timeout(30, SECONDS)

  override def receive: Receive = {
    case EvalExpr(expr) => expr match {
      case Var(value) =>  sender () ! ResExpr(value)
      case And(vals)  =>  processEval(vals, l => l.foldLeft(true)(  (s, v) => s & v))
      case Or(vals)  =>   processEval(vals, l => l.foldLeft(false)( (s, v) => s | v))
      case Xor(vals)  =>  processEval(vals, l => l.foldLeft(false)( (s, v) => s ^ v))

    }
  }

  def processEval(vals: Seq[Expr], func: IndexedSeq[Boolean] => Boolean): Unit = {
    val reqSender = sender()
    val workers = createWorkers(vals.size)

    Future.sequence(workers.zip(vals).map {
      case (worker, expr) => getResult(worker, expr)
    }) onComplete {
      case Success(results) =>
        reqSender ! ResExpr(func(results))
    }

    killWorkers(workers)
  }

  def createWorkers(num: Int) : IndexedSeq[ActorRef] =
    (0 until num).map(_ => context.system.actorOf(Props(classOf[WorkerActor])))

  def getResult(worker: ActorRef, expr: Expr): Future[Boolean] =
    (worker ? WorkerProtocol.EvalExpr(expr)).mapTo[WorkerProtocol.ResExpr].map{
      case WorkerProtocol.ResExpr(result) => result
    }

  def killWorkers(workers: IndexedSeq[ActorRef]) : Unit =
    workers.foreach(_.actorRef ! PoisonPill)

}
