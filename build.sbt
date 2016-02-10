name := "LogicEval"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= {
  val verAkka = "2.3.9"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % verAkka
  )
}
