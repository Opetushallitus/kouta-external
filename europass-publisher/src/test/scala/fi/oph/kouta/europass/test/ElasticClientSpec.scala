package fi.oph.kouta.europass.test

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.scalatra.test.scalatest.ScalatraFlatSpec
import scala.concurrent.duration._
import scala.concurrent.Await

import fi.oph.kouta.europass.ElasticClient

class ElasticClientSpec extends ScalatraFlatSpec {
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
}
