**To build artifacts**

`git clone https://github.com/scarymrgrey/spark.restconsumer.git`

`cd spark.restconsumer`

`sbt assembly`

**To submit with YARN**

`spark-submit --class CurrencyJob --master yarn --deploy-mode cluster --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.1 --driver-memory 4g --executor-memory 2g --executor-cores 1 target/scala-2.11/Spark.RestConsumer-assembly-1.0.jar` 