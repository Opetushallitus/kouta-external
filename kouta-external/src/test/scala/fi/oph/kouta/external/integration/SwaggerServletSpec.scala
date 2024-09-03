package fi.oph.kouta.external.integration
import fi.oph.kouta.external.integration.fixture.{SwaggerFixture}
import io.swagger.v3.parser.OpenAPIV3Parser

class SwaggerServletSpec extends SwaggerFixture {
  "Swagger" should "have valid spec" in {
    get("/swagger/swagger.yaml") {
      val result  = new OpenAPIV3Parser().readContents(body, null, null)
      val openApi = result.getOpenAPI()
      openApi should not equal (null)     // Parsimisen pitää onnistua (on validia YML:ää)
      result.getMessages() shouldBe empty // Ei virheitä tai varoituksia swaggerin parsinnasta
    }
  }
}
