import logic._
import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global

object LogicEvalApp extends App with ActorApi {

  val expr: Expr =
    And(Seq(
      And(Seq(
        Var(true),
        Var(false))),
      Or(Seq(
        Var(false),
        Var(true))),
      Xor(Seq(
        Var(false),
        Var(true)))
    ))

  processExprWithActors(expr) onComplete {
    case Success(result) => println(result)
  }
}
