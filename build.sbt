name := "Spark.RestConsumer"

version := "1.0"

scalaVersion := "2.12.8"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.4.4"

mainClass := Some("CurrencyJob")

libraryDependencies += "org.apache.spark" %% "spark-sql-kafka-0-10" % "2.4.4"

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.4"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.4"

libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.4.2"

libraryDependencies ++= {
  val liftVersion = "3.3.0"
  Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile",
    "ch.qos.logback" % "logback-classic" % "1.2.3"
  )
}

libraryDependencies += "com.typesafe" % "config" % "1.4.0"

resolvers += "jitpack" at "https://jitpack.io"
libraryDependencies += "com.github.mrpowers" % "spark-fast-tests" % "v0.16.0" % "test"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.1.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.0" % "test"