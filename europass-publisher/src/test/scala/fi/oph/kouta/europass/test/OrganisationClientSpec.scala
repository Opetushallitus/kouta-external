package fi.oph.kouta.europass.test

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.scalatra.test.scalatest.ScalatraFlatSpec
import scala.io.Source

import fi.oph.kouta.europass.OrganisationClient

class OrganisationClientSpec extends ScalatraFlatSpec {

  "OrganisationClient" should "extract address correctly" in {
    val orgLine = List(
      "1.2.246.562.10.81934895871", "kaynti",
      "Polvivaara 865", "posti_15110", "LAHTI", "kieli_fi#1"
    )
    assert(OrganisationClient.organisationAddress(orgLine)
      == Some("Polvivaara 865, 15110  LAHTI"))
  }

  it should "fetch organisation and extract address" in {
    assert(TestOrganisationClient.getOrganisationAddress("1.2.246.562.10.81934895871")
      == Some("Polvivaara 865, 15110  LAHTI"))
    assert(TestOrganisationClient.getOrganisationAddress("1.2.246.562.10.2013111415031319523704")
      == Some("Halivaarankatu 676, 90250  OULU"))
    assert(TestOrganisationClient.getOrganisationAddress("1.2.246.562.10.123")
      == None)
  }

  it should "write correct AWS configuration" in {
    val configFile = OrganisationClient.writeAwsConfig()
    assert(configFile.contains("aws-config"))
    val content = Source.fromFile(configFile).mkString
    assert(content.contains("role_arn = arn:aws:iam::123:role/fulldump-read-role-opintopolku"))
  }

  "TestOrganisationClient" should "fetch Organisation CSV from resources" in {
    val orgFile = TestOrganisationClient.getOrganisationCsv()
    assert(orgFile.contains("organisations"))
    val content = Source.fromFile(orgFile).mkString
    assert(content.contains("Svinhufvudinkatu 6 F"))
  }

  it should "have correct organisations from resources" in {
    val orgs = TestOrganisationClient.organisations
    assert(orgs(("1.2.246.562.10.67476956288","kaynti","fi"))(0) == "1.2.246.562.10.67476956288")
    assert(orgs(("1.2.246.562.10.594252633210","kaynti","fi"))(2) == "Volttipelto 801")
  }

}
