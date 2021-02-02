package fi.oph.kouta.external

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

import fi.oph.kouta.domain.{EiSähköistä, Fi, Julkaistu, Kielistetty, Sv}
import fi.oph.kouta.TestOids._
import fi.oph.kouta.external.domain.{Ajanjakso, Haku, HakuMetadata, Osoite, Yhteyshenkilo}

object TestData {

  def now() = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)

  def inFuture(s: Long = 500) = LocalDateTime.now().plusSeconds(s).truncatedTo(ChronoUnit.MINUTES)

  def inPast(s: Long = 500) = LocalDateTime.now().minusSeconds(s).truncatedTo(ChronoUnit.MINUTES)

  def kieliMap(text: String): Kielistetty = Map(Fi -> s"$text fi", Sv -> s"$text sv")

  def getInvalidHakuajat = List(Ajanjakso(TestData.inFuture(9000), TestData.inFuture(3000)))

  val Osoite1 = Osoite(
    osoite = Map(Fi -> "Kivatie 1", Sv -> "kivavägen 1"),
    postinumeroKoodiUri = Some("posti_04230#2"))

  val Yhteystieto1 = Yhteyshenkilo(
    nimi = Map(Fi -> "Aku Ankka", Sv -> "Aku Ankka"),
    puhelinnumero = Map(Fi -> "123", Sv -> "123"),
    sahkoposti = Map(Fi -> "aku.ankka@ankkalinnankoulu.fi", Sv -> "aku.ankka@ankkalinnankoulu.fi"),
    titteli = Map(Fi -> "titteli", Sv -> "titteli sv"),
    wwwSivu = Map(Fi -> "http://opintopolku.fi", Sv -> "http://studieinfo.fi"))

  val JulkaistuHaku = Haku(
    nimi = Map(Fi -> "Haku fi", Sv -> "Haku sv"),
    tila = Julkaistu,
    hakutapaKoodiUri = Some("hakutapa_03#1"),
    hakukohteenLiittamisenTakaraja = Some(inFuture()),
    hakukohteenMuokkaamisenTakaraja = Some(inFuture()),
    ajastettuJulkaisu = Some(inFuture()),
    kohdejoukkoKoodiUri = Some("haunkohdejoukko_17#1"),
    kohdejoukonTarkenneKoodiUri = Some("haunkohdejoukontarkenne_1#1"),
    hakulomaketyyppi = Some(EiSähköistä),
    hakulomakeAtaruId = Some(UUID.randomUUID()),
    hakulomakeKuvaus = Map(Fi -> "Hakulomake tulostetaan ja toimitetaan postitse", Sv -> "Hakulomake tulostetaan ja toimitetaan postitse sv"),
    hakulomakeLinkki = Map(Fi -> "https://koulu.test/hakemusinfo-fi", Sv -> "https://koulu.test/hakemusinfo-sv"),
    metadata = Some(HakuMetadata(Seq(Yhteystieto1), Seq(Ajanjakso(alkaa = now(), paattyy = inFuture())))),
    hakuajat = List(Ajanjakso(alkaa = now(), paattyy = inFuture())),
    organisaatioOid = ChildOid,
    muokkaaja = TestUserOid,
    kielivalinta = Seq(Fi, Sv),
    modified = None)

  val MinHaku = Haku(
    muokkaaja = TestUserOid,
    organisaatioOid = LonelyOid,
    kielivalinta = Seq(Fi, Sv),
    nimi = kieliMap("Minimi haku"),
    modified = None)
}
