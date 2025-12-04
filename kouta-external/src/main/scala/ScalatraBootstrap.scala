import fi.oph.kouta.external.KoutaConfigurationFactory
import fi.oph.kouta.external.database.KoutaDatabase
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

    context.mount(KoulutusServlet, "/koulutus", "koulutus")
    context.mount(MassKoulutusServlet, "/koulutukset", "koulutukset")
    context.mount(ValintaperusteServlet, "/valintaperuste", "valintaperuste")
    context.mount(HakuServlet, "/haku", "haku")
    context.mount(HakukohdeServlet, "/hakukohde", "hakukohde")
    context.mount(MassHakukohdeServlet, "/hakukohteet", "hakukohteet")
    context.mount(ToteutusServlet, "/toteutus", "toteutus")
    context.mount(MassToteutusServlet, "/toteutukset", "toteutukset")
    context.mount(SorakuvausServlet, "/sorakuvaus", "sorakuvaus")

    context.mount(HealthcheckServlet, "/healthcheck", "healthcheck")
    context.mount(new SwaggerServlet, "/swagger")

  }

  override def destroy(context: ServletContext): Unit = {
    super.destroy(context)
    KoutaDatabase.destroy()
  }

}
