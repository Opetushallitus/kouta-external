import fi.oph.kouta.external.KoutaConfigurationFactory
import fi.oph.kouta.external.database.KoutaDatabase
import fi.oph.kouta.external.elasticsearch.DefaultElasticsearchClientHolder
import fi.oph.kouta.external.servlet._
import fi.oph.kouta.external.swagger.SwaggerServlet
import javax.servlet.ServletContext
import org.scalatra._

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    super.init(context)

    KoutaConfigurationFactory.init()
    KoutaDatabase.init()

    context.mount(new AuthServlet(), "/auth", "auth")

    context.mount(new KoulutusServlet(DefaultElasticsearchClientHolder), "/koulutus", "koulutus")
    context.mount(new ValintaperusteServlet(DefaultElasticsearchClientHolder), "/valintaperuste", "valintaperuste")
    context.mount(new HakuServlet(DefaultElasticsearchClientHolder), "/haku", "haku")
    context.mount(new HakukohdeServlet(DefaultElasticsearchClientHolder), "/hakukohde", "hakukohde")
    context.mount(new ToteutusServlet(DefaultElasticsearchClientHolder), "/toteutus", "toteutus")

    context.mount(new HealthcheckServlet(), "/healthcheck", "healthcheck")
    context.mount(new SwaggerServlet, "/swagger")

  }

  override def destroy(context: ServletContext): Unit = {
    super.destroy(context)
    KoutaDatabase.destroy()
  }

}
