import net.liftweb.json.Serialization.write
import net.liftweb.json.{DefaultFormats, compactRender, parse}
import scalaj.http.Http
import scala.collection.immutable

class CurrencyResponseIterator(inner: Iterator[CurrencyRequest], url: String, batchSize: Int) extends Iterator[String] {

  override def hasNext: Boolean = inner.hasNext

  private var buffer: List[String] = List()

  private def getNewBatch: immutable.List[String] = {
    implicit val formats: DefaultFormats.type = DefaultFormats
    val batch = inner.take(batchSize).toArray
    val json: String = write(batch)
    val response = Http(s"$url/currency")
      .postData(json)
      .header("content-type", "application/json")
      .asString
    val resp = parse(response.body)
    resp.children.map(compactRender)
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
