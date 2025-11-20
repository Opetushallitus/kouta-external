package fi.oph.kouta.external.integration.fixture

import fi.oph.kouta.external.database.SessionDAO
import fi.oph.kouta.external.elasticsearch._
import fi.oph.kouta.external.security.{CasSessionService, SecurityContext}
import fi.oph.kouta.external.service._
import fi.oph.kouta.external.servlet._
import fi.oph.kouta.external.swagger.SwaggerServlet
import fi.oph.kouta.external._
import fi.oph.kouta.security.{CasSession, ServiceTicket}
import fi.vm.sade.properties.OphProperties
import org.scalatra.test.scalatest.ScalatraFlatSpec

import java.util.UUID

trait SwaggerFixture extends ScalatraFlatSpec {
  KoutaConfigurationFactory.setupWithDefaultTemplateFile()
  TestSetups.setupPostgres()
  val KoulutusPath       = "/koulutus"
  val ToteutusPath       = "/toteutus"
  val HakukohdePath      = "/hakukohde"
  val HakuPath           = "/haku"
  val ValintaperustePath = "/valintaperuste"
  val AuthPath           = "/auth"

  val serviceIdentifier  = KoutaIntegrationSpec.serviceIdentifier
  val rootOrganisaatio   = KoutaIntegrationSpec.rootOrganisaatio
  val defaultAuthorities = KoutaIntegrationSpec.defaultAuthorities

  val defaultSessionId = UUID.randomUUID()

  val testUser = TestUser("test-user-oid", "testuser", defaultSessionId)

  var urlProperties: Option[OphProperties] = None

  val casUrl = "testCasUrl"

  override def beforeAll(): Unit = {
    super.beforeAll()
    SessionDAO.store(CasSession(ServiceTicket(testUser.ticket), testUser.oid, defaultAuthorities), testUser.sessionId)
    urlProperties = Some(KoutaConfigurationFactory.configuration.urlProperties)
    val mockKoutaClient      = new MockKoutaClient(urlProperties.get)
    val organisaatioService  = new OrganisaatioServiceImpl(urlProperties.get)
    val hakukohderyhmaClient = new MockHakukohderyhmaClient(urlProperties.get)

    val koulutusService =
      new KoulutusService(KoulutusClient, EPerusteClient, mockKoutaClient, organisaatioService)
    addServlet(new KoulutusServlet(koulutusService), KoulutusPath)

    val toteutusService =
      new ToteutusService(ToteutusClient, mockKoutaClient, organisaatioService)
    addServlet(new ToteutusServlet(toteutusService), ToteutusPath)

    val hakuService = new HakuService(HakuClient, mockKoutaClient, organisaatioService)
    addServlet(new HakuServlet(hakuService), HakuPath)

    val hakukohderyhmaService = new HakukohderyhmaService(hakukohderyhmaClient, organisaatioService)
    val hakukohdeService = new HakukohdeService(
      HakukohdeClient,
      hakukohderyhmaService,
      new MockKoutaClient(urlProperties.get),
      organisaatioService,
      hakuService
    )
    addServlet(new HakukohdeServlet(hakukohdeService), HakukohdePath)

    val valintaperusteService = new ValintaperusteService(
      ValintaperusteClient,
      mockKoutaClient,
      organisaatioService
    )
    addServlet(new ValintaperusteServlet(valintaperusteService), ValintaperustePath)

    val securityContext: SecurityContext = MockSecurityContext(casUrl, serviceIdentifier, defaultAuthorities)
    object MockCasSessionService extends CasSessionService(securityContext)

    addServlet(new AuthServlet(MockCasSessionService), AuthPath)
    addServlet(HealthcheckServlet, "/healthcheck")
    addServlet(new SwaggerServlet(), "/swagger")
  }

  override def afterAll(): Unit = {
    super.afterAll()
  }
}
