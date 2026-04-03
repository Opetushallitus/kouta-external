import fi.oph.kouta.external.KoutaConfigurationFactory
import fi.oph.kouta.external.database.KoutaDatabase
import fi.oph.kouta.external.servlet._
import fi.oph.kouta.external.swagger.SwaggerServlet
import fi.oph.kouta.koutalight.client.SiirtotiedostoPalveluClient
import fi.oph.kouta.koutalight.repository.{KoutaExternalDatabaseConnection, KoutaLightSiirtotiedostoDAO}
import fi.oph.kouta.koutalight.service.KoutaLightSiirtotiedostoService
import org.scalatra._

import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    super.init(context)

    KoutaConfigurationFactory.init()
    KoutaDatabase.init()

    val dbConnectionConfiguration =
      KoutaConfigurationFactory.configuration.ovaraKoutaLightConfiguration.databaseConnectionConfiguration
    val s3Configuration = KoutaConfigurationFactory.configuration.ovaraKoutaLightConfiguration.s3Configuration
    val dbConnection = KoutaExternalDatabaseConnection(dbConnectionConfiguration)

    val koutaLightSiirtotiedostoDAO = new KoutaLightSiirtotiedostoDAO(dbConnection)
    val koutaLightSiirtotiedostoPalveluClient = new SiirtotiedostoPalveluClient(s3Configuration)
    val koutaLightSiirtotiedostoService = new KoutaLightSiirtotiedostoService(koutaLightSiirtotiedostoDAO, koutaLightSiirtotiedostoPalveluClient)

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

    context.mount(
      KoutaLightServlet,
      "/koutan-tietomallista-poikkeavat-koulutukset",
      "koutan-tietomallista-poikkeavat-koulutukset"
    )

    context.mount(
      new KoutaLightSiirtotiedostoServlet(koutaLightSiirtotiedostoService),
      "/siirtotiedosto",
      "siirtotiedosto"
    )
    context.mount(new SwaggerServlet, "/swagger")

  }

  override def destroy(context: ServletContext): Unit = {
    super.destroy(context)
    KoutaDatabase.destroy()
  }

}
