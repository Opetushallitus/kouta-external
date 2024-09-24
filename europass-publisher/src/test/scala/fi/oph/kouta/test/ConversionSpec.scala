package fi.oph.kouta.test

import scala.io.Source
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.scalatra.test.scalatest.ScalatraFlatSpec

class ConversionSpec extends ScalatraFlatSpec {
  implicit val formats = DefaultFormats
  lazy val example_toteutus: JValue = parse(
    Source.fromResource("toteutus-example-1.json").bufferedReader
  )
  //val example_toteutus : JValue = new JString("foo")

  "example_toteutus" should "have correct fields" in {
    assert((example_toteutus \ "koulutusOid").extract[String] == "1.2.246.562.13.00000000000000000001")
    assert((example_toteutus \ "nimi" \ "sv").extract[String] == "Glusiska haren 2022")
    assert(((example_toteutus \ "hakutiedot")(0) \ "hakuOid").extract[String] == "1.2.246.562.29.00000000000000000001")
  }
}
