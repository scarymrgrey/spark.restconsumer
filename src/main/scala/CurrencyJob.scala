
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object CurrencyJob {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load("application.conf").getConfig("spark")
    implicit val spark: SparkSession =
      SparkSession.builder
        .master("local")
        .appName("Currency converter")
        .getOrCreate()
    val kafkaURI = config.getString("kafka-cluster")
    val checkpointDir = config.getString("checkpoint-path")
    val df = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaURI)
      .option("subscribe", "currency_requests")
      .option("startingOffsets", "earliest")
      .load()

    val stream = CurrencyConverter(new HttpClientImpl(config.getString("currency-api")))
      .convertCurrency(df)
      .select(to_json(struct("id", "value", "from_currency", "to_currency"))
        .alias("value"))
      .writeStream
      .format("kafka")
      .outputMode("append")
      .option("kafka.bootstrap.servers", kafkaURI)
      .option("topic", "currency_requests_alpakka")
      .option("checkpointLocation", checkpointDir)
      .start()

    stream.awaitTermination()
    spark.stop()
  }
}

