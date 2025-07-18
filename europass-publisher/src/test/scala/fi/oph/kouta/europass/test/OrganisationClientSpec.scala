package fi.oph.kouta.europass.test

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.scalatra.test.scalatest.ScalatraFlatSpec
import scala.io.Source

import fi.oph.kouta.europass.OrganisationClient

object TestOrganisationClient extends OrganisationClient {
  override def getOrganisation(oid: String): JValue =
    parse(Source.fromResource(s"organisaatio-$oid.json").bufferedReader)
}

class OrganisationClientSpec extends ScalatraFlatSpec {

  "OrganisationClient" should "extract address correctly" in {
    val orgJson = parse(
      Source.fromResource("organisaatio-1.2.246.562.10.81934895871.json").bufferedReader
    )
    assert(OrganisationClient.organisationAddress(orgJson)
      == Some("Polvivaara 865, 15110  LAHTI"))
  }

  it should "fetch organisation and extract address" in {
    assert(TestOrganisationClient.getOrganisationAddress("1.2.246.562.10.81934895871")
      == Some("Polvivaara 865, 15110  LAHTI"))
    assert(TestOrganisationClient.getOrganisationAddress("1.2.246.562.10.2013111415031319523704")
      == None)
  }

}
