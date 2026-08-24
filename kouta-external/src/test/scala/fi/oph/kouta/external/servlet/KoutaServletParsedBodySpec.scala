package fi.oph.kouta.external.servlet

import fi.oph.kouta.external.KoutaConfigurationFactory
import org.json4s.JValue
import org.json4s.jackson.JsonMethods.{compact, parse, render}
import org.scalatra.Ok
import org.scalatra.test.scalatest.ScalatraFlatSpec

/** json4s 4.x hylkää eksplisiittisen JSON-nullin luokalle, jolla on Option-kenttiä, kun taas 3.6:ssa
  * se purkautui None:ksi. `KoutaServlet.parsedBody` normalisoi nullit pois, jotta rajapinta
  * hyväksyy samat pyyntörungot kuin ennen — ja samat kuin kouta-backend, jossa on vastaava
  * sanitointi.
  */
class KoutaServletParsedBodySpec extends ScalatraFlatSpec {
  KoutaConfigurationFactory.setupWithDefaultTemplateFile()

  private class EchoServlet extends KoutaServlet {
    post("/echo") {
      Ok(compact(render(parsedBody)))
    }
  }

  addServlet(new EchoServlet, "/*")

  private def echo(rawJson: String)(check: JValue => Unit): Unit =
    post("/echo", rawJson.getBytes("UTF-8"), Map("Content-Type" -> "application/json")) {
      status shouldBe 200
      check(parse(body))
    }

  "parsedBody" should "drop explicit nulls from objects, recursively" in {
    echo("""{"nimi":null,"metadata":{"esittely":null,"opintojenLaajuusNumero":10}}""") { parsed =>
      compact(render(parsed)) shouldBe """{"metadata":{"opintojenLaajuusNumero":10}}"""
    }
  }

  it should "leave null-free bodies untouched" in {
    echo("""{"tila":"julkaistu","tarjoajat":["1.2.246.562.10.1"]}""") { parsed =>
      compact(render(parsed)) shouldBe """{"tila":"julkaistu","tarjoajat":["1.2.246.562.10.1"]}"""
    }
  }

  /** Dokumentoitu poikkeama masteriin: `noNulls` suodattaa myös taulukoiden null-alkiot. */
  it should "also drop null elements from arrays" in {
    echo("""{"tarjoajat":["1.2.246.562.10.1",null,"1.2.246.562.10.2"]}""") { parsed =>
      compact(render(parsed)) shouldBe """{"tarjoajat":["1.2.246.562.10.1","1.2.246.562.10.2"]}"""
    }
  }
}
