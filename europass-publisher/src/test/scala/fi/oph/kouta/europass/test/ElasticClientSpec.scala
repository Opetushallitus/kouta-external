package fi.oph.kouta.europass.test

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.scalatra.test.scalatest.ScalatraFlatSpec
import scala.concurrent.duration._
import scala.concurrent.Await

import fi.oph.kouta.europass.ElasticClient
import fi.oph.kouta.external.domain.indexed.ToteutusIndexed

class ElasticClientSpec extends ScalatraFlatSpec with ElasticFixture {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit val formats = DefaultFormats

  "elasticsearch" should "respond" in {
    assert(ElasticClient.testConnection())
  }

  "example toteutus" should "be loadable" in {
    val tot = ElasticClient.getToteutus("1.2.246.562.17.00000000000000000002")(0)
    assert(tot.tila.name == "julkaistu")
    assert(tot.koulutusOid.map(_.toString).getOrElse("")
      == "1.2.246.562.13.00000000000000000001")
  }

  "published toteutukset" should "have both toteutukset" in {
    val result = ElasticClient.listPublished()
    assert(result.toArray.length == 2)
    assert(result.map{tot => tot.oid.toString}.toList ==
      List("1.2.246.562.17.00000000000000000001", "1.2.246.562.17.00000000000000000002"))
  }
}
