package fi.oph.kouta.external.domain.indexed

import com.fasterxml.jackson.annotation.{JsonCreator, JsonProperty}
import fi.oph.kouta.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid, UserOid}
import fi.oph.kouta.domain.{Alkamiskausityyppi, En, Fi, Hakulomaketyyppi, Julkaisutila, Kieli, Modified, Sv}
import fi.oph.kouta.external.domain._
import fi.vm.sade.utils.slf4j.Logging

import java.time.{LocalDateTime, ZoneOffset}
import java.util.UUID

case class EmbeddedToteutusIndexed(tarjoajat: List[Organisaatio])
case class EmbeddedHakukohdeIndexed(
    oid: HakukohdeOid,
    jarjestyspaikka: Option[Organisaatio],
    toteutus: EmbeddedToteutusIndexed,
    tila: Julkaisutila
)

case class EmbeddedHakukohdeIndexedES @JsonCreator() (
    @JsonProperty("oid") oid: String,
    @JsonProperty("jarjestyspaikka") jarjestyspaikka: Option[Object],
    @JsonProperty("toteutus") toteutus: EmbeddedToteutusIndexedES,
    @JsonProperty("tila") tila: String
)
case class EmbeddedToteutusIndexedES @JsonCreator() (
    @JsonProperty("tarjoajat") tarjoajat: Object
)
case class HakuMetadataES @JsonCreator() (
    @JsonProperty("yhteyshenkilot") yhteyshenkilot: Seq[YhteyshenkiloES],
    @JsonProperty("tulevaisuudenAikataulu") tulevaisuudenAikataulu: List[AikaJakso],
    @JsonProperty("koulutuksenAlkamiskausi") koulutuksenAlkamiskausi: Option[KoulutuksenAlkamiskausiHakukohdeES]
)

case class YhteyshenkiloES @JsonCreator() (
    @JsonProperty("nimi") nimi: Map[String, String],
    @JsonProperty("puhelinnumero") puhelinnumero: Map[String, String],
    @JsonProperty("sahkoposti") sahkoposti: Map[String, String],
    @JsonProperty("titteli") titteli: Map[String, String],
    @JsonProperty("wwwSivu") wwwSivu: Map[String, String],
    @JsonProperty("wwwSivuTeksti") wwwSivuTeksti: Map[String, String]
)

case class HakuTapaES @JsonCreator() (
    @JsonProperty("koodiUri") koodiUri: String,
    @JsonProperty("nimi") nimi: Map[String, String]
)

case class KohdejoukkoES @JsonCreator() (
    @JsonProperty("koodiUri") koodiUri: String,
    @JsonProperty("nimi") nimi: Map[String, String]
)

case class KohdejoukonTarkenneES @JsonCreator() (
    @JsonProperty("koodiUri") koodiUri: String,
    @JsonProperty("nimi") nimi: Map[String, String]
)

case class HakuJavaClient @JsonCreator() (
    @JsonProperty("oid") oid: String,
    @JsonProperty("externalId") externalId: String,
    @JsonProperty("tila") tila: String,
    @JsonProperty("nimi") nimi: Map[String, String],
    @JsonProperty("hakukohteet") hakukohteet: List[EmbeddedHakukohdeIndexedES],
    @JsonProperty("hakutapa") hakutapa: HakuTapaES,
    @JsonProperty("hakukohteenLiittamisenTakaraja") hakukohteenLiittamisenTakaraja: String,
    @JsonProperty("hakukohteenMuokkaamisenTakaraja") hakukohteenMuokkaamisenTakaraja: String,
    @JsonProperty("ajastettuJulkaisu") ajastettuJulkaisu: String,
    @JsonProperty("kohdejoukko") kohdejoukko: KohdejoukkoES,
    @JsonProperty("kohdejoukonTarkenne") kohdejoukonTarkenne: KohdejoukonTarkenneES,
    @JsonProperty("hakulomaketyyppi") hakulomaketyyppi: String,
    @JsonProperty("hakulomakeAtaruId") hakulomakeAtaruId: String,
    @JsonProperty("hakulomakeKuvaus") hakulomakeKuvaus: Map[String, String],
    @JsonProperty("hakulomakeLinkki") hakulomakeLinkki: Map[String, String],
    @JsonProperty("metadata") metadata: Option[HakuMetadataES],
    @JsonProperty("organisaatio") organisaatio: OrganisaatioES,
    @JsonProperty("hakuajat") hakuajat: List[AikaJakso],
    @JsonProperty("valintakokeet") valintakokeet: List[ValintakoeES],
    @JsonProperty("muokkaaja") muokkaaja: MuokkaajaES,
    @JsonProperty("kielivalinta") kielivalinta: Seq[String],
    @JsonProperty("modified") modified: String
) {

  def toResult(): HakuIndexed = {
    HakuIndexed(
      oid = Option.apply(oid).map(oid => HakuOid(oid)),
      externalId = Option.apply(externalId),
      tila = if (tila != null) Julkaisutila.withName(tila) else null,
      nimi = toKielistettyMap(nimi),
      hakukohteet = createHakukohteet(hakukohteet),
      hakutapa = Option.apply(if (hakutapa != null) KoodiUri(hakutapa.koodiUri) else null),
      hakukohteenLiittamisenTakaraja = Option.apply(parseLocalDateTime(hakukohteenLiittamisenTakaraja)),
      hakukohteenMuokkaamisenTakaraja = Option.apply(parseLocalDateTime(hakukohteenMuokkaamisenTakaraja)),
      ajastettuJulkaisu = Option.apply(parseLocalDateTime(ajastettuJulkaisu)),
      kohdejoukko = Option.apply(KoodiUri(kohdejoukko.koodiUri)),
      kohdejoukonTarkenne =
        Option.apply(if (kohdejoukonTarkenne != null) KoodiUri(kohdejoukonTarkenne.koodiUri) else null),
      hakulomaketyyppi = Option.apply(Hakulomaketyyppi.withName(hakulomaketyyppi)),
      hakulomakeAtaruId = Option.apply(hakulomakeAtaruId).map(hakulomakeAtaruId => UUID.fromString(hakulomakeAtaruId)),
      hakulomakeKuvaus = toKielistettyMap(hakulomakeKuvaus),
      hakulomakeLinkki = toKielistettyMap(hakulomakeLinkki),
      metadata = getHakuMetadataIndexed(metadata),
      organisaatio = if (organisaatio != null) Organisaatio(oid = OrganisaatioOid(organisaatio.oid)) else null,
      hakuajat =
        if (hakuajat != null) hakuajat.map(hakuaika => {
          Ajanjakso(parseLocalDateTime(hakuaika.alkaa), Option.apply(parseLocalDateTime(hakuaika.paattyy)))
        })
        else List.empty,
      valintakokeet = getValintakokeet(valintakokeet),
      muokkaaja = if (muokkaaja != null) Muokkaaja(UserOid(muokkaaja.oid)) else null,
      kielivalinta = if (kielivalinta != null) kielivalinta.map(kieli => Kieli.withName(kieli)) else Seq.empty,
      modified = if (modified != null) Option.apply(Modified(LocalDateTime.parse(modified))) else None
    )
  }

  def getValintakokeet(valintakoeList: List[ValintakoeES]): Option[List[ValintakoeIndexed]] = {
    if (valintakoeList != null) {
      Option.apply(valintakoeList.map(koe => {
        ValintakoeIndexed(
          id = Option.apply(UUID.fromString(koe.id)),
          tyyppi = Option.apply(if (koe.tyyppi != null) KoodiUri(koe.tyyppi.koodiUri) else null),
          nimi = toKielistettyMap(koe.nimi),
          metadata = Option.apply(
            ValintaKoeMetadataIndexed(
              tietoja = toKielistettyMap(koe.metadata.tietoja),
              vahimmaispisteet = Option.apply(koe.metadata.vahimmaispisteet),
              liittyyEnnakkovalmistautumista = Option.apply(koe.metadata.liittyyEnnakkovalmistautumista),
              ohjeetEnnakkovalmistautumiseen = toKielistettyMap(koe.metadata.ohjeetEnnakkovalmistautumiseen),
              erityisjarjestelytMahdollisia = Option.apply(koe.metadata.erityisjarjestelytMahdollisia),
              ohjeetErityisjarjestelyihin = toKielistettyMap(koe.metadata.ohjeetErityisjarjestelyihin)
            )
          ),
          tilaisuudet = koe.tilaisuudet.map(tilaisuus => {

            ValintakoetilaisuusIndexed(
              osoite = Option.apply(
                OsoiteIndexed(
                  osoite = toKielistettyMap(tilaisuus.osoite.osoite),
                  postinumero = Option.apply(KoodiUri(tilaisuus.osoite.postinumeroKoodiUri))
                )
              ),
              aika = Option.apply(
                Ajanjakso(
                  alkaa = if (tilaisuus.aika != null) parseLocalDateTime(tilaisuus.aika.alkaa) else null,
                  paattyy =
                    if (tilaisuus.aika != null) Option.apply(parseLocalDateTime(tilaisuus.aika.paattyy)) else null
                )
              ),
              lisatietoja = toKielistettyMap(tilaisuus.lisatietoja),
              jarjestamispaikka = toKielistettyMap(tilaisuus.jarjestamispaikka)
            )
          })
        )
      }))
    } else Option.apply(List.empty)

  }
  def getHakuMetadataIndexed(metadataES: Option[HakuMetadataES]): Option[HakuMetadataIndexed] = {
    if (metadataES != null) {
      val koulutuksenAlkamiskausiOption = metadataES.get.koulutuksenAlkamiskausi
      Option.apply(
        HakuMetadataIndexed(
          yhteyshenkilot = metadataES.get.yhteyshenkilot.map(m =>
            Yhteyshenkilo(
              nimi = toKielistettyMap(m.nimi),
              titteli = toKielistettyMap(m.titteli),
              sahkoposti = toKielistettyMap(m.sahkoposti),
              puhelinnumero = toKielistettyMap(m.puhelinnumero),
              wwwSivu = toKielistettyMap(m.wwwSivu),
              Option.apply(toKielistettyMap(m.wwwSivuTeksti))
            )
          ),
          tulevaisuudenAikataulu = metadataES.get.tulevaisuudenAikataulu.map(m =>
            Ajanjakso(
              parseLocalDateTime(m.alkaa),
              Option.apply(parseLocalDateTime(m.paattyy))
            )
          ),
          koulutuksenAlkamiskausi = koulutuksenAlkamiskausiOption.map(koulutuksenAlkamiskausi =>
            KoulutuksenAlkamiskausiIndexed(
              alkamiskausityyppi =
                Option.apply(Alkamiskausityyppi.withName(koulutuksenAlkamiskausi.alkamiskausityyppi)),
              henkilokohtaisenSuunnitelmanLisatiedot =
                toKielistettyMap(koulutuksenAlkamiskausi.henkilokohtaisenSuunnitelmanLisatiedot),
              koulutuksenAlkamispaivamaara =
                Option.apply(parseLocalDateTime(koulutuksenAlkamiskausi.koulutuksenAlkamispaivamaara)),
              koulutuksenPaattymispaivamaara =
                Option.apply(parseLocalDateTime(koulutuksenAlkamiskausi.koulutuksenPaattymispaivamaara)),
              koulutuksenAlkamiskausi = Option.apply(
                if (koulutuksenAlkamiskausi.koulutuksenAlkamiskausi != null)
                  KoodiUri(koulutuksenAlkamiskausi.koulutuksenAlkamiskausi.koodiUri)
                else null
              ),
              koulutuksenAlkamisvuosi = Option.apply(koulutuksenAlkamiskausi.koulutuksenAlkamisvuosi)
            )
          )
        )
      )
    } else None
  }

  def createHakukohteet(list: List[EmbeddedHakukohdeIndexedES]): List[EmbeddedHakukohdeIndexed] = {
    if (list != null)
      list.map(m =>
        EmbeddedHakukohdeIndexed(
          oid = HakukohdeOid(m.oid),
          jarjestyspaikka = None,
          toteutus = null,
          tila = Julkaisutila.withName(m.tila)
        )
      )
    else List.empty
  }
  def parseLocalDateTime(dateString: String): LocalDateTime = {
    if (dateString != null) LocalDateTime.parse(dateString) else null
  }
  def toKielistettyMap(map: Map[String, String]): Kielistetty = {
    if (map != null) {
      Map(
        En -> map.get("en"),
        Fi -> map.get("fi"),
        Sv -> map.get("sv")
      ).collect { case (k, Some(v)) => (k, v) }
    } else {
      Map.empty
    }
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
