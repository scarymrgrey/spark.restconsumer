
import org.apache.spark.sql.SparkSession

trait SparkSessionTestWrapper {
  implicit lazy val spark: SparkSession = {
    SparkSession.builder
      .master("local[*]")
      .appName("Currency converter test")
      .getOrCreate()
  }

}