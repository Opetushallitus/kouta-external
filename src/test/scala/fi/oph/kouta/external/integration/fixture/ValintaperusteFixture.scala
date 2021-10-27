package fi.oph.kouta.external.integration.fixture

import fi.oph.kouta.domain.{Julkaisutila, Kieli, Koulutustyyppi, Modified}

import java.util.UUID
import fi.oph.kouta.domain.oid.{OrganisaatioOid, UserOid}
import fi.oph.kouta.external.KoutaFixtureTool.{ExternalIdKey, HakutapaKoodiUriKey, JulkinenKey, KielivalintaKey, KohdejoukkoKoodiUriKey, KoulutustyyppiKey, MetadataKey, ModifiedKey, MuokkaajaKey, NimiKey, OrganisaatioKey, TilaKey, ValintakokeetKey, ValintaperusteIdKey}
import fi.oph.kouta.external.domain.{Valintakoe, Valintaperuste, ValintaperusteMetadata}
import fi.oph.kouta.external.elasticsearch.ValintaperusteClient
import fi.oph.kouta.external.service.{OrganisaatioServiceImpl, ValintaperusteService}
import fi.oph.kouta.external.servlet.ValintaperusteServlet
import fi.oph.kouta.external.{KoutaFixtureTool, TempElasticClient}
import org.json4s.jackson.{Serialization}

import java.time.LocalDateTime

trait ValintaperusteFixture extends KoutaIntegrationSpec with AccessControlSpec {
  val ValintaperustePath = "/valintaperuste"

  override def beforeAll(): Unit = {
    super.beforeAll()
    val organisaatioService = new OrganisaatioServiceImpl(urlProperties.get)
    val valintaperusteService = new ValintaperusteService(new ValintaperusteClient(TempElasticClient.client), organisaatioService)
    addServlet(new ValintaperusteServlet(valintaperusteService), ValintaperustePath)
  }

  def get(id: UUID): Valintaperuste = get[Valintaperuste](ValintaperustePath, id)

  def get(id: UUID, sessionId: UUID): Valintaperuste = get[Valintaperuste](ValintaperustePath, id, sessionId)

  def get(id: UUID, sessionId: UUID, errorStatus: Int): Unit = get(s"$ValintaperustePath/$id", sessionId, errorStatus)

  def get(id: UUID, sessionId: UUID, expected: Valintaperuste): String =
    get[Valintaperuste, UUID](ValintaperusteIdKey, id, sessionId, expected)

  def addMockValintaperuste(
      id: UUID,
      organisaatioOid: OrganisaatioOid,
      modifier: Map[String, String] => Map[String, String] = identity
  ): Valintaperuste = {
    val valintaperuste = KoutaFixtureTool.DefaultValintaperusteScala +
      (KoutaFixtureTool.OrganisaatioKey -> organisaatioOid.s)
    KoutaFixtureTool.addValintaperuste(id.toString, modifier(valintaperuste))
    indexValintaperuste(id)
    toValintaperuste(id, modifier(valintaperuste))
  }

  def toValintaperuste(id: UUID, raw: Map[String, String]): Valintaperuste =
    {
      val kielivalinta: Seq[Kieli] = raw(KielivalintaKey).split(",").map(_.trim).map(Kieli.withName)
      Valintaperuste(Some(id),
        externalId = raw.get(ExternalIdKey),
        tila = Julkaisutila.withName(raw.get(TilaKey).get),
        koulutustyyppi = Koulutustyyppi.withName(raw.get(KoulutustyyppiKey).get),
        hakutapaKoodiUri = raw.get(HakutapaKoodiUriKey),
        kohdejoukkoKoodiUri = raw.get(KohdejoukkoKoodiUriKey),
        nimi = kielivalinta.map { k => (k, raw(NimiKey) + " " + k.toString) }.toMap,
        julkinen = raw.get(JulkinenKey).get.toBoolean,
        valintakokeet = raw.get(ValintakokeetKey).map(Serialization.read[List[Valintakoe]]).get,
        metadata = raw.get(MetadataKey).map(Serialization.read[ValintaperusteMetadata]),
        organisaatioOid = OrganisaatioOid(raw.get(OrganisaatioKey).get),
        muokkaaja = UserOid(raw.get(MuokkaajaKey).get),
        kielivalinta = kielivalinta,
        modified = Some(Modified(LocalDateTime.from(ISO_MODIFIED_FORMATTER.parse(raw.get(ModifiedKey).get)))))
    }
}
