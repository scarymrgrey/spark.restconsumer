import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{IntegerType, StringType, StructType}


case class CurrencyRequest(id: String, value: Double, from_currency: String, to_currency: String)

case class CurrencyResponse(id: String, initial: Double, converted: Double, from_currency: String, to_currency: String)

object BotDetector extends App {

  val spark = SparkSession.builder
    .master("local[4]")
    .appName("Bot Detector")
    .config("spark.driver.memory", "2g")
    .getOrCreate()

  val rootLogger = Logger.getRootLogger
  rootLogger.setLevel(Level.ERROR)

  Logger.getLogger("org.apache.spark").setLevel(Level.ERROR)
  Logger.getLogger("org.spark-project").setLevel(Level.ERROR)

  import spark.implicits._

  val schema = new StructType()
    .add("value", IntegerType)
    .add("from_currency", StringType)
    .add("to_currency", StringType)
  val checkpointDir = "file:///Users/kpolunin/checkpoint/chkpnt335"
  val df = spark
    .readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("subscribe", "currency_requests")
    .option("startingOffsets", "earliest")
    .load()

  val uuid = udf(() => java.util.UUID.randomUUID().toString)
  val currency_requests =
    df
      .select(from_json($"value".cast("string"), schema).as("value"))
      .selectExpr("value.*")
      .withColumn("id", uuid())
      .as[CurrencyRequest]

  val currency_responses = currency_requests.mapPartitions(requests => {
    new CurrencyResponseIterator(requests)
  })
  currency_responses
    .writeStream
    .format("kafka")
    .outputMode("append")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("topic", "currency_responses")
    .option("checkpointLocation", checkpointDir)
    .start()
    .awaitTermination()
}

