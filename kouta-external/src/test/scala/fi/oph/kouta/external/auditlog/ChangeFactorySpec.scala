package fi.oph.kouta.external.auditlog

import com.google.gson.JsonParser
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ChangeFactorySpec extends AnyFlatSpec with Matchers {

  private def changes(before: String, after: String): String =
    ChangeFactory.getChanges(JsonParser.parseString(before), JsonParser.parseString(after)).asJsonArray().toString

  "ChangeFactory" should "return no changes for identical json" in {
    changes("""{"tila":"julkaistu","nimi":{"fi":"a"}}""", """{"tila":"julkaistu","nimi":{"fi":"a"}}""") shouldBe "[]"
  }

  it should "return updated nested field with dotted path" in {
    changes("""{"nimi":{"fi":"vanha","sv":"x"}}""", """{"nimi":{"fi":"uusi","sv":"x"}}""") shouldBe
      """[{"fieldName":"nimi.fi","oldValue":"vanha","newValue":"uusi"}]"""
  }

  it should "return added and removed fields" in {
    changes("""{"nimi":{"fi":"a"},"tila":"julkaistu"}""", """{"nimi":{"sv":"b"},"tila":"julkaistu"}""") shouldBe
      """[{"fieldName":"nimi.fi","oldValue":"a"},{"fieldName":"nimi.sv","newValue":"b"}]"""
  }

  it should "treat null as missing value" in {
    changes("""{"metadata":{"aloituspaikat":null}}""", """{"metadata":{"aloituspaikat":10}}""") shouldBe
      """[{"fieldName":"metadata.aloituspaikat","newValue":"10"}]"""
  }

  it should "compare arrays as whole values" in {
    changes("""{"kielivalinta":["fi"]}""", """{"kielivalinta":["fi","sv"]}""") shouldBe
      """[{"fieldName":"kielivalinta","oldValue":"[\"fi\"]","newValue":"[\"fi\",\"sv\"]"}]"""
  }
}
