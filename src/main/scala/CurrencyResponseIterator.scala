import net.liftweb.json.Serialization.write
import net.liftweb.json.{DefaultFormats, parse}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class CurrencyResponseIterator(inner: Iterator[CurrencyRequest],
                               httpClient: HttpClientTrait,
                               batchSize: Int) extends Iterator[CurrencyResponse] with Serializable {

  override def hasNext: Boolean = inner.hasNext || buffer.nonEmpty

  private var buffer: List[CurrencyResponse] = List()

  private def getNewBatch: List[CurrencyResponse] = {
    implicit val ec = ExecutionContext.global
    implicit val formats: DefaultFormats.type = DefaultFormats

    def lift[T](futures: Seq[Future[T]]): Seq[Future[Try[T]]] =
      futures.map(_.map {
        Success(_)
      }.recover { case t => Failure(t) })

    val tasks = inner.take(batchSize)
      .toList
      .map { z =>
        Future {
          val json = write(Array(z))
          val response = httpClient.post(json)
          parse(response)
        }
      }

    val succeededTasks = Await.result(Future.sequence(lift(tasks)), Duration.Inf)
      .filterNot(_.isFailure)
      .map(z => z.get.extract[CurrencyResponse])

    succeededTasks.toList
  }

  override def next(): CurrencyResponse = {
    buffer match {
      case head :: tail =>
        buffer = tail
        head
      case head :: Nil =>
        buffer = getNewBatch
        head
      case Nil =>
        val newBatch = getNewBatch
        buffer = if (newBatch.length == 1) Nil else newBatch.tail
        newBatch.head
    }
  }
}
