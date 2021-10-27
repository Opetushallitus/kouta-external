package fi.oph.kouta.external.integration.fixture

import java.util.UUID
import fi.oph.kouta.TestOids._
import fi.oph.kouta.domain.{Julkaisutila, Kieli, Koulutustyyppi, Modified}
import fi.oph.kouta.domain.oid.{KoulutusOid, OrganisaatioOid, UserOid}
import fi.oph.kouta.external.KoutaFixtureTool.{EPerusteIdKey, ExternalIdKey, JohtaaTutkintoonKey, JulkinenKey, KielivalintaKey, KoulutuksetKoodiUriKey, KoulutustyyppiKey, MetadataKey, ModifiedKey, MuokkaajaKey, NimiKey, OrganisaatioKey, SorakuvausIdKey, TarjoajatKey, TeemakuvaKey, TilaKey}
import fi.oph.kouta.external.domain.{Kielistetty, Koulutus, KoulutusMetadata}
import fi.oph.kouta.external.elasticsearch.KoulutusClient
import fi.oph.kouta.external.service.{KoulutusService, OrganisaatioServiceImpl}
import fi.oph.kouta.external.servlet.KoulutusServlet
import fi.oph.kouta.external.{KoutaFixtureTool, TempElasticClient}
import org.json4s.jackson.Serialization.read

import java.time.LocalDateTime

trait KoulutusFixture extends KoutaIntegrationSpec with AccessControlSpec {
  val KoulutusPath = "/koulutus"

  override def beforeAll(): Unit = {
    super.beforeAll()
    val organisaatioService = new OrganisaatioServiceImpl(urlProperties.get)
    val koulutusService     = new KoulutusService(new KoulutusClient(TempElasticClient.client), organisaatioService)
    addServlet(new KoulutusServlet(koulutusService), KoulutusPath)
  }

  def get(oid: KoulutusOid): Koulutus = get[Koulutus](KoulutusPath, oid)

  def get(oid: KoulutusOid, sessionId: UUID): Koulutus = get[Koulutus](KoulutusPath, oid, sessionId)

  def get(oid: KoulutusOid, sessionId: UUID, errorStatus: Int): Unit =
    get(s"$KoulutusPath/$oid", sessionId, errorStatus)

  def get(oid: KoulutusOid, sessionId: UUID, expected: Koulutus): String =
    get[Koulutus, KoulutusOid](KoulutusPath, oid, sessionId, expected)

  def addMockKoulutus(
      koulutusOid: KoulutusOid,
      sorakuvausId: UUID,
      organisaatioOid: OrganisaatioOid = ChildOid,
      modifier: Map[String, String] => Map[String, String] = identity
  ): Koulutus = {
    val koulutus = KoutaFixtureTool.DefaultKoulutusScala +
      (KoutaFixtureTool.OrganisaatioKey -> organisaatioOid.s) +
      (KoutaFixtureTool.SorakuvausIdKey -> sorakuvausId.toString)
    KoutaFixtureTool.addKoulutus(koulutusOid.s, modifier(koulutus))
    indexKoulutus(koulutusOid)
    toKoulutus(koulutusOid, modifier(koulutus))
  }

  def addMockSorakuvaus(id: UUID, organisaatioOid: OrganisaatioOid): Unit = {
    val sorakuvaus = KoutaFixtureTool.DefaultSorakuvausScala + (KoutaFixtureTool.OrganisaatioKey -> organisaatioOid.s)
    KoutaFixtureTool.addSorakuvaus(id.toString, sorakuvaus)
    indexSorakuvaus(id)
  }

  def toKoulutus(oid: KoulutusOid, raw: Map[String, String]): Koulutus = {
    val externalId: Option[String] = raw.get(ExternalIdKey)
    val johtaaTutkintoon: Boolean = raw.get(JohtaaTutkintoonKey).get.toBoolean
    val koulutustyyppi: Koulutustyyppi = Koulutustyyppi.withName(raw.get(KoulutustyyppiKey).get)
    val koulutuksetKoodiUri: Seq[String] = raw.get(KoulutuksetKoodiUriKey) match {
      case None | Some(null) => Seq()
      case Some(x) => x.split(",").map(_.trim).toSeq
    }
    val tila: Julkaisutila = Julkaisutila.withName(raw.get(TilaKey).get)
    val tarjoajat: List[OrganisaatioOid] = raw.get(TarjoajatKey) match {
      case None => List[OrganisaatioOid]()
      case Some(x) if x.trim == "" => List[OrganisaatioOid]()
      case Some(x) => x.split(",").map(_.trim).map(OrganisaatioOid).toList
    }
    val julkinen: Boolean = raw.get(JulkinenKey).get.toBoolean
    val kielivalinta: Seq[Kieli] = raw(KielivalintaKey).split(",").map(_.trim).map(Kieli.withName)
    val nimi: Kielistetty = kielivalinta.map { k => (k, raw(NimiKey) + " " + k.toString) }.toMap
    val metadata: Option[KoulutusMetadata] = raw.get(MetadataKey).map(read[KoulutusMetadata])
    val sorakuvausId: Option[UUID] = Some(UUID.fromString(raw.get(SorakuvausIdKey).get))
    val muokkaaja: UserOid = UserOid(raw.get(MuokkaajaKey).get)
    val organisaatioOid: OrganisaatioOid = OrganisaatioOid(raw.get(OrganisaatioKey).get)
    val teemakuva: Option[String] = raw.get(TeemakuvaKey)
    val ePerusteId: Option[Long] = raw.get(EPerusteIdKey) match {
      case None | Some(null) => None
      case Some(x) => Some(x.toLong)
    }
    val modified: Option[Modified] =
      Some(Modified(LocalDateTime.from(ISO_MODIFIED_FORMATTER.parse(raw.get(ModifiedKey).get))))
    Koulutus(
      Some(oid), externalId, johtaaTutkintoon, koulutustyyppi, koulutuksetKoodiUri, tila, tarjoajat, julkinen,
      kielivalinta, nimi, metadata, sorakuvausId, muokkaaja, organisaatioOid, teemakuva, ePerusteId, modified)
  }
}
