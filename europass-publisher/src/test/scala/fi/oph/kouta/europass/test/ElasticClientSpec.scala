package fi.oph.kouta.europass.test

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.scalatra.test.scalatest.ScalatraFlatSpec
import scala.io.Source

import fi.oph.kouta.europass.ElasticClient
import fi.oph.kouta.external.domain.indexed.ToteutusIndexed

object TestElasticClient extends ElasticClient {
  var elasticCallCount = 0;
  lazy val exampleJson: JValue = parse(
    Source.fromResource("list-toteutukset.json").bufferedReader
  )
  override def postJson[T <: AnyRef](urlSuffix: String, body: T): JValue = {
    elasticCallCount = elasticCallCount + 1;
    exampleJson
  }
}

class ElasticClientSpec extends ScalatraFlatSpec with ElasticFixture {
  implicit val formats = DefaultFormats

  "elasticsearch" should "respond" in {
    val res1 = ElasticClient.getJson("")
    assert((res1 \ "tagline").extract[String] == "You Know, for Search")
    val res2 = ElasticClient.getJson("_cluster/health")
    assert((res2 \ "status").extract[String] == "yellow")
  }

  "example toteutus" should "be loadable" in {
    val tot = ElasticClient.getToteutus("1.2.246.562.17.00000000000000000002")
    assert(tot.tila.name == "julkaistu")
    assert(tot.koulutusOid.map(_.toString).getOrElse("")
      == "1.2.246.562.13.00000000000000000001")
  }

  "example koulutus" should "be loadable" in {
    val kou = ElasticClient.getKoulutus("1.2.246.562.13.00000000000000000006")
    assert(kou.koulutukset(0).koodiUri == "koulutus_371101#1")
    assert(kou.tila.name == "julkaistu")
  }

  "published toteutukset" should "have both toteutukset" in {
    val result = ElasticClient.listPublished(None)
    assert(result.toArray.length == 2)  // testidatan molemmat toteutukset ovat julkaistuja
    assert(result.map{tot => tot.oid.map(_.toString).getOrElse("")}.toList ==
      List("1.2.246.562.17.00000000000000000001", "1.2.246.562.17.00000000000000000002"))
  }

  "listPublished" should "be lazy" in {
    val callCountBefore = TestElasticClient.elasticCallCount
    val resultList: Stream[ToteutusIndexed] = TestElasticClient.listPublished(None)
    assert(TestElasticClient.elasticCallCount == callCountBefore + 1)
    assert(resultList.take(3).map(_.tila.name) == List("julkaistu", "julkaistu", "julkaistu"))
    assert(TestElasticClient.elasticCallCount == callCountBefore + 3)
  }
}
