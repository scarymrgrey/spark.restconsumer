name := "Spark.RestConsumer"

version := "0.1"

scalaVersion := "2.11.8"

libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.4.3" % "provided"

mainClass := Some("CurrencyJob")

libraryDependencies += "org.apache.spark" %% "spark-sql-kafka-0-10" % "2.4.3" % "provided"

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.3" % "provided"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.3" % "provided"

libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.4.2" % "provided"

libraryDependencies ++= {
  val liftVersion = "3.3.0"
  Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile",
    "ch.qos.logback" % "logback-classic" % "1.2.3"
  )
}

libraryDependencies += "com.typesafe" % "config" % "1.4.0" % "provided"

resolvers += "jitpack" at "https://jitpack.io"
libraryDependencies += "com.github.mrpowers" % "spark-fast-tests" % "v0.16.0" % "test"
//libraryDependencies += "org.scalactic" %% "scalactic" % "3.1.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.0" % "test"