package fi.oph.kouta.europass.test

import org.scalatra.test.scalatest.ScalatraFlatSpec
import scala.concurrent.duration._
import scala.concurrent.Await
import java.io._

import fi.oph.kouta.europass.Publisher

class PublisherSpec extends ScalatraFlatSpec {

  "publisher" should "create correct toteutusXml from ElasticSearch" in {
    val writer = new StringWriter()
    Await.result(
      Publisher.toteutusToFile(
        "1.2.246.562.17.00000000000000000001", new BufferedWriter(writer)),
      60.second)
    assert(writer.toString.contains("<loq:contentUrl>https://opintopolku.fi/konfo/sv/toteutus/1.2.246.562.17.00000000000000000001</loq:contentUrl>"))
    assert(writer.toString.contains("<loq:providedBy idref=\"https://rdf.oph.fi/organisaatio/1.2.246.562.10.594252633210\"/>"))

    // Want to have the test XML as a file?  Here you go:
    // val w = new BufferedWriter(new FileWriter("test.txt"))
    // w.write(writer.toString)
    // w.close()

  }

}

