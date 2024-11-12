package fi.oph.kouta.external.domain.indexed

import com.fasterxml.jackson.annotation.{JsonCreator, JsonProperty}
import fi.oph.kouta.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid, UserOid}
import fi.oph.kouta.domain.{Alkamiskausityyppi, En, Fi, Hakulomaketyyppi, Julkaisutila, Kieli, Modified, Sv}
import fi.oph.kouta.external.domain._
import fi.oph.kouta.logging.Logging

import java.time.{LocalDateTime, ZoneOffset}
import java.util.UUID
import scala.util.Try

case class EmbeddedToteutusIndexed(tarjoajat: List[Organisaatio])
case class EmbeddedHakukohdeIndexed(
    oid: HakukohdeOid,
    jarjestyspaikka: Option[Organisaatio],
    toteutus: EmbeddedToteutusIndexed,
    tila: Julkaisutila
)

case class EmbeddedHakukohdeIndexedES @JsonCreator() (
    @JsonProperty("oid") oid: String,
    @JsonProperty("jarjestyspaikka") jarjestyspaikka: Option[OrganisaatioES],
    @JsonProperty("toteutus") toteutus: EmbeddedToteutusIndexedES,
    @JsonProperty("tila") tila: String
)
case class EmbeddedToteutusIndexedES @JsonCreator() (
    @JsonProperty("tarjoajat") tarjoajat: List[OrganisaatioES] = List()
)
case class HakuMetadataES @JsonCreator() (
    @JsonProperty("yhteyshenkilot") yhteyshenkilot: Seq[YhteyshenkiloES] = Seq(),
    @JsonProperty("tulevaisuudenAikataulu") tulevaisuudenAikataulu: List[AikaJakso] = List(),
    @JsonProperty("koulutuksenAlkamiskausi") koulutuksenAlkamiskausi: Option[KoulutuksenAlkamiskausiHakukohdeES]
)

case class YhteyshenkiloES @JsonCreator() (
    @JsonProperty("nimi") nimi: Map[String, String] = Map(),
    @JsonProperty("puhelinnumero") puhelinnumero: Map[String, String] = Map(),
    @JsonProperty("sahkoposti") sahkoposti: Map[String, String] = Map(),
    @JsonProperty("titteli") titteli: Map[String, String] = Map(),
    @JsonProperty("wwwSivu") wwwSivu: Map[String, String] = Map(),
    @JsonProperty("wwwSivuTeksti") wwwSivuTeksti: Map[String, String] = Map()
)

case class HakuTapaES @JsonCreator() (
    @JsonProperty("koodiUri") koodiUri: String,
    @JsonProperty("nimi") nimi: Map[String, String] = Map()
)

case class KohdejoukkoES @JsonCreator() (
    @JsonProperty("koodiUri") koodiUri: String,
    @JsonProperty("nimi") nimi: Map[String, String] = Map()
)

case class KohdejoukonTarkenneES @JsonCreator() (
    @JsonProperty("koodiUri") koodiUri: String,
    @JsonProperty("nimi") nimi: Map[String, String] = Map()
)

case class HakuJavaClient @JsonCreator() (
    @JsonProperty("oid") oid: Option[String],
    @JsonProperty("externalId") externalId: Option[String],
    @JsonProperty("tila") tila: String,
    @JsonProperty("nimi") nimi: Map[String, String] = Map(),
    @JsonProperty("hakukohteet") hakukohteet: List[EmbeddedHakukohdeIndexedES] = List(),
    @JsonProperty("hakutapa") hakutapa: Option[HakuTapaES],
    @JsonProperty("hakukohteenLiittamisenTakaraja") hakukohteenLiittamisenTakaraja: Option[String],
    @JsonProperty("hakukohteenMuokkaamisenTakaraja") hakukohteenMuokkaamisenTakaraja: Option[String],
    @JsonProperty("hakukohteenLiittajaOrganisaatiot") hakukohteenLiittajaOrganisaatiot: Seq[String],
    @JsonProperty("ajastettuJulkaisu") ajastettuJulkaisu: Option[String],
    @JsonProperty("kohdejoukko") kohdejoukko: Option[KohdejoukkoES],
    @JsonProperty("kohdejoukonTarkenne") kohdejoukonTarkenne: Option[KohdejoukonTarkenneES],
    @JsonProperty("hakulomaketyyppi") hakulomaketyyppi: Option[String],
    @JsonProperty("hakulomakeAtaruId") hakulomakeAtaruId: Option[String],
    @JsonProperty("hakulomakeKuvaus") hakulomakeKuvaus: Map[String, String] = Map(),
    @JsonProperty("hakulomakeLinkki") hakulomakeLinkki: Map[String, String] = Map(),
    @JsonProperty("metadata") metadata: Option[HakuMetadataES],
    @JsonProperty("organisaatio") organisaatio: OrganisaatioES,
    @JsonProperty("hakuajat") hakuajat: List[AikaJakso] = List(),
    @JsonProperty("valintakokeet") valintakokeet: List[ValintakoeES] = List(),
    @JsonProperty("muokkaaja") muokkaaja: MuokkaajaES,
    @JsonProperty("kielivalinta") kielivalinta: Seq[String] = Seq(),
    @JsonProperty("modified") modified: Option[String]
) {

  def toResult(): HakuIndexed = {
    HakuIndexed(
      oid = oid.map(HakuOid),
      externalId = externalId,
      tila = Julkaisutila.withName(tila),
      nimi = toKielistettyMap(nimi),
      hakukohteet = createHakukohteet(hakukohteet),
      hakutapa = hakutapa.map(h => KoodiUri(h.koodiUri)),
      hakukohteenLiittamisenTakaraja = hakukohteenLiittamisenTakaraja.map(parseLocalDateTime),
      hakukohteenMuokkaamisenTakaraja = hakukohteenMuokkaamisenTakaraja.map(parseLocalDateTime),
      hakukohteenLiittajaOrganisaatiot = hakukohteenLiittajaOrganisaatiot.map(OrganisaatioOid),
      ajastettuJulkaisu = ajastettuJulkaisu.map(parseLocalDateTime),
      kohdejoukko = kohdejoukko.map(kj => KoodiUri(kj.koodiUri)),
      kohdejoukonTarkenne = kohdejoukonTarkenne.map(kjt => KoodiUri(kjt.koodiUri)),
      hakulomaketyyppi = hakulomaketyyppi.map(Hakulomaketyyppi.withName),
      hakulomakeAtaruId = hakulomakeAtaruId.map(UUID.fromString),
      hakulomakeKuvaus = toKielistettyMap(hakulomakeKuvaus),
      hakulomakeLinkki = toKielistettyMap(hakulomakeLinkki),
      metadata = getHakuMetadataIndexed(metadata),
      organisaatio = Organisaatio(oid = OrganisaatioOid(organisaatio.oid)),
      hakuajat = hakuajat.map(hakuaika => {
        Ajanjakso(parseLocalDateTime(hakuaika.alkaa), hakuaika.paattyy.map(parseLocalDateTime))
      }),
      valintakokeet = getValintakokeet(valintakokeet),
      muokkaaja = Muokkaaja(UserOid(muokkaaja.oid)),
      kielivalinta = kielivalinta.map(kieli => Kieli.withName(kieli)),
      modified = modified.map(m => Modified(LocalDateTime.parse(m)))
    )
  }

  def getValintakokeet(valintakoeList: List[ValintakoeES]): Option[List[ValintakoeIndexed]] = {
    Some(valintakoeList.map(koe => {
      ValintakoeIndexed(
        id = Try(UUID.fromString(koe.id)).toOption,
        tyyppi = koe.tyyppi.map(tyyppi => KoodiUri(tyyppi.koodiUri)),
        nimi = toKielistettyMap(koe.nimi),
        metadata = koe.metadata.map(metadata =>
          ValintaKoeMetadataIndexed(
            tietoja = toKielistettyMap(metadata.tietoja),
            vahimmaispisteet = metadata.vahimmaispisteet,
            liittyyEnnakkovalmistautumista = metadata.liittyyEnnakkovalmistautumista,
            ohjeetEnnakkovalmistautumiseen = toKielistettyMap(metadata.ohjeetEnnakkovalmistautumiseen),
            erityisjarjestelytMahdollisia = metadata.erityisjarjestelytMahdollisia,
            ohjeetErityisjarjestelyihin = toKielistettyMap(metadata.ohjeetErityisjarjestelyihin)
          )
        ),
        tilaisuudet = koe.tilaisuudet.map(tilaisuus => {

          ValintakoetilaisuusIndexed(
            osoite = tilaisuus.osoite.map(_.toKielistettyOsoite),
            aika = tilaisuus.aika.map(aika => Ajanjakso(
              alkaa = parseLocalDateTime(aika.alkaa),
              paattyy = aika.paattyy.map(paattyy => parseLocalDateTime(paattyy))
            )),
            lisatietoja = toKielistettyMap(tilaisuus.lisatietoja),
            jarjestamispaikka = toKielistettyMap(tilaisuus.jarjestamispaikka)
          )
        })
      )
    }))

  }
  def getHakuMetadataIndexed(metadataESOption: Option[HakuMetadataES]): Option[HakuMetadataIndexed] = {
    metadataESOption.map(metadataES =>
      HakuMetadataIndexed(
        yhteyshenkilot = metadataES.yhteyshenkilot.map(m =>
          Yhteyshenkilo(
            nimi = toKielistettyMap(m.nimi),
            titteli = toKielistettyMap(m.titteli),
            sahkoposti = toKielistettyMap(m.sahkoposti),
            puhelinnumero = toKielistettyMap(m.puhelinnumero),
            wwwSivu = toKielistettyMap(m.wwwSivu),
            wwwSivuTeksti = Some(toKielistettyMap(m.wwwSivuTeksti))
          )
        ),
        tulevaisuudenAikataulu = metadataES.tulevaisuudenAikataulu.map(m =>
          Ajanjakso(
            parseLocalDateTime(m.alkaa),
            m.paattyy.map(parseLocalDateTime)
          )
        ),
        koulutuksenAlkamiskausi = metadataES.koulutuksenAlkamiskausi.map(koulutuksenAlkamiskausi =>
          KoulutuksenAlkamiskausiIndexed(
            alkamiskausityyppi = koulutuksenAlkamiskausi.alkamiskausityyppi.map(Alkamiskausityyppi.withName),
            henkilokohtaisenSuunnitelmanLisatiedot =
              toKielistettyMap(koulutuksenAlkamiskausi.henkilokohtaisenSuunnitelmanLisatiedot),
            koulutuksenAlkamispaivamaara = koulutuksenAlkamiskausi.koulutuksenAlkamispaivamaara.map(parseLocalDateTime),
            koulutuksenPaattymispaivamaara = koulutuksenAlkamiskausi.koulutuksenPaattymispaivamaara.map(parseLocalDateTime),
            koulutuksenAlkamiskausi = koulutuksenAlkamiskausi.koulutuksenAlkamiskausi.map(ka => KoodiUri(ka.koodiUri)),
            koulutuksenAlkamisvuosi = koulutuksenAlkamiskausi.koulutuksenAlkamisvuosi
          )
        )
      )
    )
  }

  def createHakukohteet(hakukohteet: List[EmbeddedHakukohdeIndexedES]): List[EmbeddedHakukohdeIndexed] = {
    hakukohteet.map(m =>
      EmbeddedHakukohdeIndexed(
        oid = HakukohdeOid(m.oid),
        jarjestyspaikka = m.jarjestyspaikka.map(j => Organisaatio(OrganisaatioOid(j.oid))),
        toteutus = EmbeddedToteutusIndexed(m.toteutus.tarjoajat.map(t => Organisaatio(OrganisaatioOid(t.oid)))),
        tila = Julkaisutila.withName(m.tila)
      )
    )
  }
  def parseLocalDateTime(dateString: String): LocalDateTime = {
    LocalDateTime.parse(dateString)
  }
  def toKielistettyMap(map: Map[String, String]): Kielistetty = {
    Map(
      En -> map.get("en"),
      Fi -> map.get("fi"),
      Sv -> map.get("sv")
    ).collect { case (k, Some(v)) => (k, v) }
  }
}

case class HakuIndexed(
    oid: Option[HakuOid],
    externalId: Option[String],
    tila: Julkaisutila,
    nimi: Kielistetty,
    hakukohteet: List[EmbeddedHakukohdeIndexed],
    hakutapa: Option[KoodiUri],
    hakukohteenLiittamisenTakaraja: Option[LocalDateTime],
    hakukohteenMuokkaamisenTakaraja: Option[LocalDateTime],
    hakukohteenLiittajaOrganisaatiot: Seq[OrganisaatioOid],
    ajastettuJulkaisu: Option[LocalDateTime],
    kohdejoukko: Option[KoodiUri],
    kohdejoukonTarkenne: Option[KoodiUri],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeAtaruId: Option[UUID],
    hakulomakeKuvaus: Kielistetty,
    hakulomakeLinkki: Kielistetty,
    metadata: Option[HakuMetadataIndexed],
    organisaatio: Organisaatio,
    hakuajat: List[Ajanjakso],
    valintakokeet: Option[List[ValintakoeIndexed]],
    muokkaaja: Muokkaaja,
    kielivalinta: Seq[Kieli],
    modified: Option[Modified]
) extends WithTila
    with Logging {
  def toHaku(): Haku = {
    def getHakukausiUri(ajanjakso: Ajanjakso): String = {
      ajanjakso.paattyy.map(_.getMonthValue).getOrElse {
        ajanjakso.alkaa.getMonthValue
      }
    } match {
      case m if m >= 1 && m <= 7  => "kausi_k#1"
      case m if m >= 8 && m <= 12 => "kausi_s#1"
      case _                      => ""
    }

    try {
      Haku(
        oid = oid,
        hakukohdeOids = Some(hakukohteet.map(_.oid)),
        tila = tila,
        nimi = nimi,
        hakutapaKoodiUri = hakutapa.map(_.koodiUri),
        hakukohteenLiittamisenTakaraja = hakukohteenLiittamisenTakaraja,
        hakukohteenMuokkaamisenTakaraja = hakukohteenMuokkaamisenTakaraja,
        hakukohteenLiittajaOrganisaatiot = hakukohteenLiittajaOrganisaatiot,
        ajastettuJulkaisu = ajastettuJulkaisu,
        alkamiskausiKoodiUri = metadata.flatMap(m =>
          m.toHakuMetadata.koulutuksenAlkamiskausi
            .flatMap(_.koulutuksenAlkamiskausiKoodiUri)
        ),
        hakuvuosi = hakuajat
          .sortBy(ha => ha.alkaa)
          .headOption
          .map(ha => ha.paattyy.map(_.getYear).getOrElse(ha.alkaa.getYear)),
        hakukausi = hakuajat
          .sortBy(ha => ha.alkaa)
          .headOption
          .map(getHakukausiUri),
        alkamisvuosi =
          metadata.flatMap(m => m.toHakuMetadata.koulutuksenAlkamiskausi.flatMap(_.koulutuksenAlkamisvuosi)),
        kohdejoukkoKoodiUri = kohdejoukko.map(_.koodiUri),
        kohdejoukonTarkenneKoodiUri = kohdejoukonTarkenne.map(_.koodiUri),
        hakulomaketyyppi = hakulomaketyyppi,
        hakulomakeAtaruId = hakulomakeAtaruId,
        hakulomakeKuvaus = hakulomakeKuvaus,
        hakulomakeLinkki = hakulomakeLinkki,
        metadata = metadata.map(_.toHakuMetadata),
        organisaatioOid = organisaatio.oid,
        hakuajat = hakuajat,
        valintakokeet = valintakokeet.map(vl => vl.map(_.toValintakoe)),
        muokkaaja = muokkaaja.oid,
        kielivalinta = kielivalinta,
        modified = modified,
        externalId = externalId
      )
    } catch {
      case e: Exception => {
        val msg: String = s"Failed to create Haku (${oid})"
        logger.error(msg, e)
        throw new RuntimeException(msg, e)
      }
    }
  }
  implicit val localDateTimeOrdering: Ordering[LocalDateTime] = Ordering.by(_.toEpochSecond(ZoneOffset.UTC))

}

case class HakuMetadataIndexed(
    yhteyshenkilot: Seq[Yhteyshenkilo],
    tulevaisuudenAikataulu: Seq[Ajanjakso],
    koulutuksenAlkamiskausi: Option[KoulutuksenAlkamiskausiIndexed]
) {
  def toHakuMetadata: HakuMetadata = HakuMetadata(
    yhteyshenkilot = yhteyshenkilot,
    tulevaisuudenAikataulu = tulevaisuudenAikataulu,
    koulutuksenAlkamiskausi = koulutuksenAlkamiskausi.map(_.toKoulutuksenAlkamiskausi)
  )
}
