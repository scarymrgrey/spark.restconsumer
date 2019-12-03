
**To build artifacts**

`git clone https://github.com/scarymrgrey/spark.restconsumer.git`

`cd spark.restconsumer`

**Conf setup**

Please adjust your configuration in 'resources/application.conf'

` currency-api = "http://localhost:9000"`

`kafka-cluster = "localhost:9092"`
   
`checkpoint-path = "/tmp/spark-rest-checkpoint"`
   
`batch-size = 1000`

**Build**

`sbt assembly`

**To submit with YARN**

`spark-submit --class CurrencyJob --master yarn --deploy-mode cluster --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.1 --driver-memory 4g --executor-memory 2g --executor-cores 1 target/scala-2.11/Spark.RestConsumer-assembly-1.0.jar` 

**Topics setup**

Please create two topics in your kafka cluster: `currency_requests` `currency_responses`