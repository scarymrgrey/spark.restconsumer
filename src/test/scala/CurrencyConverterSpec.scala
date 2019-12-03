import net.liftweb.json.{DefaultFormats, parse}

import org.scalatest._
import org.scalatest.matchers.should.Matchers._

object TestHttpClient extends HttpClientTrait {
  val httpTrafficStub = Map(
    "{\"value\":10,\"from_currency\":\"USD\",\"to_currency\":\"PLN\"}"
      -> "{\"id\":\"7207395b-205f-4f7b-a97e-8c88a08cf7a6\",\"initial\":10,\"converted\":41,\"from_currency\":\"USD\",\"to_currency\":\"PLN\"}",
    "{\"value\":33,\"from_currency\":\"PLN\",\"to_currency\":\"USD\"}"
      -> "{\"id\":\"9b33e60a-f4fc-4aaf-9b39-c02df9a691d2\",\"initial\":33,\"converted\":8.048780487804878,\"from_currency\":\"PLN\",\"to_currency\":\"USD\"}",
    "{\"value\":330,\"from_currency\":\"PLN\",\"to_currency\":\"USD\"}"
      -> "{\"id\":\"3e7ce8c2-5ffb-410e-87f2-c47c44e82cd4\",\"initial\":330,\"converted\":80.48780487804879,\"from_currency\":\"PLN\",\"to_currency\":\"USD\"}"
  )
  lazy val requestsStub: Seq[String] = httpTrafficStub.keys.toSeq

  lazy val requestsTypedStub: Iterator[CurrencyRequest] = httpTrafficStub
    .keys.map(z => {
    implicit val formats: DefaultFormats.type = DefaultFormats
    val value = parse(z) merge parse("{\"id\":\"id\"}")
    value.extract[CurrencyRequest]
  }).toIterator
  lazy val responsesStub: Seq[CurrencyResponse] = httpTrafficStub
    .values
    .map(z => {
      implicit val formats: DefaultFormats.type = DefaultFormats
      parse(z).extract[CurrencyResponse]
    }).toSeq

  override def post(jsonString: String): String = {
    implicit val formats: DefaultFormats.type = DefaultFormats
    val req = parse(jsonString)
    val amount = (req \\ "value").children.head.extract[Int].toString
    httpTrafficStub.values.filter(p => p.contains("\"initial\":" + amount)).head
  }
}

class CurrencyConverterSpec
  extends FlatSpec
    with SparkSessionTestWrapper {

  "Iterator" should "iterate all collection" in {
    val requests = TestHttpClient.requestsTypedStub.toList.toIterator
    val iterator = new CurrencyResponseIterator(requests, TestHttpClient, 100)
    assert(iterator sameElements TestHttpClient.responsesStub.toIterator)
  }

  "CurrencyConverter" should "convert and contains all fields" in {
    import spark.implicits._

    val sourceDF = TestHttpClient.requestsStub
      .toDF("value")
    val actualDF = CurrencyConverter(TestHttpClient).convertCurrency(sourceDF)

    val expectedData = TestHttpClient.responsesStub
    val expectedDF = spark.sparkContext.parallelize(expectedData).toDS()
    assert(actualDF.collect() sameElements expectedDF.collect())
  }

  "CurrencyConverter" should "ignore invalid json strings" in {
    import spark.implicits._

    val sourceDF = (TestHttpClient.requestsStub ++ List("invalid json"))
      .toDF("value")
    val actualDF = CurrencyConverter(TestHttpClient).convertCurrency(sourceDF)

    val expectedData = TestHttpClient.responsesStub
    val expectedDF = spark.sparkContext.parallelize(expectedData).toDS()

    assert(actualDF.collect() sameElements expectedDF.collect())
  }
}