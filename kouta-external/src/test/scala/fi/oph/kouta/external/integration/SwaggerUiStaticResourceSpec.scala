package fi.oph.kouta.external.integration

import fi.oph.kouta.external.swagger.SwaggerServlet
import jakarta.servlet.ServletContext
import org.eclipse.jetty.ee10.webapp.WebAppContext
import org.eclipse.jetty.server.{Server, ServerConnector}
import org.eclipse.jetty.util.resource.ResourceFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatra._
import org.scalatra.servlet.ScalatraListener

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}

/**
 * Mountataan vain SwaggerServlet, jotta testi ei tarvitse tietokantaa eikä konfiguraatiota.
 * ScalatraListener etsii tämän luokan konteksti-parametrista org.scalatra.LifeCycle
 * ScalatraBootstrapin sijaan.
 */
class SwaggerUiTestBootstrap extends LifeCycle {
  override def init(context: ServletContext): Unit =
    context.mount(new SwaggerServlet, "/swagger")
}

/**
 * Swagger-UI:n staattiset tiedostot tarjoillaan Scalatran notFound-fallbackin kautta, joka
 * forwardaa nimetylle "default"-servletille. Jetty 12:ssa se toimii vain jos
 * pathInfoOnly on webapp/WEB-INF/web.xml:ssä pakotettu falseksi. Testi nostaa aidon
 * WebAppContextin samalla tavalla kuin JettyLauncher ja lukee tuotannon web.xml:n, koska
 * ScalatraFlatSpecin addServlet-pohjainen kontainer ei sisällä default-servlettiä lainkaan
 * eikä siksi kattaisi tätä polkua.
 */
class SwaggerUiStaticResourceSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll {
  private val ContextPath = "/kouta-external"

  private val httpClient = HttpClient.newHttpClient()

  private var server: Server             = _
  private var connector: ServerConnector = _

  override def beforeAll(): Unit = {
    super.beforeAll()

    val context      = new WebAppContext()
    val baseResource = ResourceFactory.of(context).newClassLoaderResource("webapp")
    context.setBaseResource(baseResource)
    context.setDescriptor(baseResource.resolve("WEB-INF/web.xml").getURI.toString)
    context.setContextPath(ContextPath)
    context.setInitParameter(ScalatraListener.LifeCycleKey, classOf[SwaggerUiTestBootstrap].getName)

    server = new Server()
    connector = new ServerConnector(server)
    connector.setPort(0)
    server.addConnector(connector)
    server.setHandler(context)
    server.start()
  }

  override def afterAll(): Unit = {
    if (server != null) {
      server.stop()
    }
    super.afterAll()
  }

  private def get(path: String): HttpResponse[String] = {
    val request = HttpRequest
      .newBuilder(URI.create(s"http://localhost:${connector.getLocalPort}$ContextPath$path"))
      .GET()
      .build()
    httpClient.send(request, HttpResponse.BodyHandlers.ofString())
  }

  "Swagger UI" should "serve index.html from the webapp/swagger directory" in {
    val response = get("/swagger/index.html")
    response.statusCode() shouldBe 200
    response.body() should include("swagger-ui-bundle.js")
  }

  it should "serve index.html as the welcome file of /swagger/" in {
    val response = get("/swagger/")
    response.statusCode() shouldBe 200
    response.body() should include("""<div id="swagger-ui">""")
    // Jos pathInfoOnly jää trueksi, polku resolvoituu webapp-juureen ja Jetty listaa sen sisällön
    response.body() should not include "WEB-INF"
  }

  it should "serve the Swagger UI bundle and stylesheet" in {
    get("/swagger/swagger-ui-bundle.js").statusCode() shouldBe 200
    get("/swagger/swagger-ui.css").statusCode() shouldBe 200
  }

  it should "still route swagger.yaml to SwaggerServlet instead of the static fallback" in {
    val response = get("/swagger/swagger.yaml")
    response.statusCode() shouldBe 200
    response.body().trim should startWith("openapi:")
  }
}
