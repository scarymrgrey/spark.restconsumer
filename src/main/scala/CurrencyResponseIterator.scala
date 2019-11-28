import net.liftweb.json.Serialization.write
import net.liftweb.json.{DefaultFormats, compactRender, parse}
import scalaj.http.Http
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class CurrencyResponseIterator(inner: Iterator[CurrencyRequest], url: String, batchSize: Int) extends Iterator[String] {

  override def hasNext: Boolean = inner.hasNext

  private var buffer: List[String] = List()

  private def getNewBatch: List[String] = {
    implicit val ec = ExecutionContext.global
    implicit val formats: DefaultFormats.type = DefaultFormats

    def lift[T](futures: Seq[Future[T]]): Seq[Future[Try[T]]] =
      futures.map(_.map {
        Success(_)
      }.recover { case t => Failure(t) })

    val tasks = inner.take(batchSize).map { z =>
      Future {
        val json: String = write(Array(z))
        val response = Http(s"$url/currency")
          .postData(json)
          .header("content-type", "application/json")
          .asString
        compactRender(parse(response.body))
      }
    }.toSeq

    val succeededTasks = Await.result(Future.sequence(lift(tasks)), Duration.Inf)
      .filterNot(_.isFailure)
      .map(z => z.get)

    succeededTasks.toList
  }

  override def next(): String = {
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
