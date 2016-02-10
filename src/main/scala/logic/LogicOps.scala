package logic

trait Expr
case class Var(value: Boolean)   extends Expr
case class And( vals: Seq[Expr]) extends Expr
case class Or(  vals: Seq[Expr]) extends Expr
case class Xor( vals: Seq[Expr]) extends Expr

object LogicOps {
  def eval(e: Expr): Boolean = e match {
    case Var(value) => value
    case And(vals)  => vals.foldLeft(true)( (s, v) => s & eval(v) )
    case Or(vals)   => vals.foldLeft(false)( (s, v) => s | eval(v) )
    case Xor(vals)  => vals.foldLeft(false)( (s, v) => s ^ eval(v) )
  }
}
