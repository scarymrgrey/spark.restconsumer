import scalaj.http.Http

class HttpClientImpl(url: String) extends HttpClientTrait {
  override def post(jsonString: String): String = Http(s"$url/currency")
    .postData(jsonString)
    .header("content-type", "application/json")
    .asString
    .body
}
