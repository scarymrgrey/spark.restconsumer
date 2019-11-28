import net.liftweb.json.Serialization.write
import net.liftweb.json.{DefaultFormats, compactRender, parse}
import scalaj.http.Http

class CurrencyResponseIterator(inner: Iterator[CurrencyRequest]) extends Iterator[String] {

  override def hasNext: Boolean = inner.hasNext

  override def next(): String = {
    implicit val formats: DefaultFormats.type = DefaultFormats
    val request: CurrencyRequest = inner.next()
    val json: String = write(Array(request))
    val response = Http("http://localhost:9000/currency")
      .postData(json)
      .header("content-type", "application/json")
      .asString
    val resp = parse(response.body)
    compactRender(resp.children.head)
  }
}
