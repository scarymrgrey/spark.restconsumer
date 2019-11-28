name := "Spark.RestConsumer"

version := "0.1"

scalaVersion := "2.11.8"

libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.4.3"

libraryDependencies += "org.apache.spark" %% "spark-sql-kafka-0-10" % "2.4.3"

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.3"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.3"

libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "2.4.2"

libraryDependencies ++= {
  val liftVersion = "3.3.0"
  Seq(
    "net.liftweb"       %% "lift-webkit" % liftVersion % "compile",
    "ch.qos.logback" % "logback-classic" % "1.2.3"
  )
}