package sttp.tapir.examples

import io.circe.generic.auto._
import sttp.client3.{TryHttpURLConnectionBackend, UriContext}
import sttp.tapir._
import sttp.tapir.client.sttp.SttpClientInterpreter
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.mockserver.SttpMockServerClient

object    MockServerExample extends App {
  val backend = TryHttpURLConnectionBackend()

  case class SampleIn(name: String, age: Int)

  case class SampleOut(greeting: String)

  val sampleEndpoint = endpoint.post
    .in("api" / "v1" / "sample")
    .in(header[String]("X-RequestId"))
    .in(jsonBody[SampleIn])
    .errorOut(stringBody)
    .out(jsonBody[SampleOut])

  val mockServerClient = SttpMockServerClient(baseUri = uri"http://localhost:1080", backend)

  val sampleIn = "request-id-123" -> SampleIn("John", 23)
  val sampleOut = SampleOut("Hello, John!")

  val expectation = mockServerClient
    .whenInputMatches(sampleEndpoint)(sampleIn)
    .thenSuccess(sampleOut)

  println(s"Got expectation $expectation")

  val result = SttpClientInterpreter
    .toRequest(sampleEndpoint, baseUri = Some(uri"http://localhost:1080"))
    .apply(sampleIn)
    .send(backend)

  println(s"Got result $result")
}
