
import com.typesafe.config.ConfigFactory
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object CurrencyJob {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load("application.conf").getConfig("spark")
    implicit val spark: SparkSession =
    SparkSession.builder
      //.master("local")
      .appName("Currency converter")
      .getOrCreate()

    val checkpointDir = config.getString("checkpoint-path")
    val df = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "currency_requests")
      .option("startingOffsets", "earliest")
      .load()

    val stream = CurrencyConverter(new HttpClientImpl(config.getString("currency-api")))
      .convertCurrency(df)
      .select(to_json(struct("id", "from_currency", "initial", "converted", "to_currency"))
        .alias("value"))
      .writeStream
      .format("kafka")
      .outputMode("append")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("topic", "currency_responses")
      .option("checkpointLocation", checkpointDir)
      .start()

    stream.awaitTermination()
    spark.stop()
  }
}

