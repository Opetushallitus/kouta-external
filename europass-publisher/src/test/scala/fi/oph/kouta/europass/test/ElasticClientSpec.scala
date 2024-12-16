package fi.oph.kouta.europass.test

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.scalatra.test.scalatest.ScalatraFlatSpec
import scala.concurrent.duration._
import scala.concurrent.Await

import fi.oph.kouta.europass.ElasticClient

class ElasticClientSpec extends ScalatraFlatSpec with ElasticFixture {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit val formats = DefaultFormats

  "elasticsearch" should "respond" in {
    Await.result(ElasticClient.getJson("")
      .map{_ \ "tagline"}
      .map{_.extract[String] == "You Know, for Search"}
      .map{assert(_)}, 60.second)
    Await.result(ElasticClient.getJson("_cluster/health")
      .map{_ \ "status"}
      .map{_.extract[String] == "yellow"}
      .map{assert(_)}, 60.second)
  }

  "example toteutus" should "be loadable" in {
    Await.result(ElasticClient.getToteutus("1.2.246.562.17.00000000000000000002")
      .map{result: JValue => assert((result \ "tila").extract[String]
        == "julkaistu")}, 60.second)
    Await.result(ElasticClient.getToteutus("1.2.246.562.17.00000000000000000002")
      .map{result: JValue => assert((result \ "koulutusOid").extract[String]
        == "1.2.246.562.13.00000000000000000001")}, 60.second)
  }

  "published toteutukset" should "have both toteutukset" in {
    val result = ElasticClient.listPublished(None)
    assert(result.toArray.length == 2)
    assert(result.map(_ \ "oid").map(_.extract[String]).toList ==
      List("1.2.246.562.17.00000000000000000001", "1.2.246.562.17.00000000000000000002"))
  }
}
