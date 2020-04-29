package fi.oph.kouta.external

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

import fi.oph.kouta.external.TestOids._
import fi.oph.kouta.external.domain._
import fi.oph.kouta.external.domain.enums._
import fi.oph.kouta.external.domain.oid._

import scala.util.Random

object TestOids {
  val OphOid = OrganisaatioOid("1.2.246.562.10.00000000001")
  val ParentOid = OrganisaatioOid("1.2.246.562.10.594252633210")
  val ChildOid = OrganisaatioOid("1.2.246.562.10.81934895871")
  val EvilChildOid = OrganisaatioOid("1.2.246.562.10.66634895871")
  val GrandChildOid = OrganisaatioOid("1.2.246.562.10.67603619189")
  val EvilGrandChildOid = OrganisaatioOid("1.2.246.562.10.66603619189")
  val EvilCousin = OrganisaatioOid("1.2.246.562.10.66634895666")

  val LonelyOid = OrganisaatioOid("1.2.246.562.10.99999999999")
  val UnknownOid = OrganisaatioOid("1.2.246.562.10.99999999998")
  val YoOid = OrganisaatioOid("1.2.246.562.10.46312206843")
  val AmmOid = OrganisaatioOid("1.2.246.562.10.463122068666")
  val OtherOid = OrganisaatioOid("1.2.246.562.10.67476956288")

  val TestUserOid = UserOid("1.2.246.562.24.10000000000")

  val random = new Random()

  private def elevenRandomDigits: String = List.fill(11)(random.nextInt(10)).map(_.toString).mkString
  private def randomOid(identifier: String) = s"1.2.246.562.$identifier.$elevenRandomDigits"

  def randomUserOid: UserOid = UserOid(randomOid("24"))
  def randomOrganisaatioOid: OrganisaatioOid = OrganisaatioOid(randomOid("10"))
}

object TestData {

  def now() = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)

  def inFuture(s: Long = 500) = LocalDateTime.now().plusSeconds(s).truncatedTo(ChronoUnit.MINUTES)

  def inPast(s: Long = 500) = LocalDateTime.now().minusSeconds(s).truncatedTo(ChronoUnit.MINUTES)

  def kieliMap(text: String): Kielistetty = Map(Kieli.Fi -> s"$text fi", Kieli.Sv -> s"$text sv")

  def getInvalidHakuajat = List(Ajanjakso(TestData.inFuture(9000), TestData.inFuture(3000)))

  val Osoite1 = Osoite(
    osoite = Map(Kieli.Fi -> "Kivatie 1", Kieli.Sv -> "kivavägen 1"),
    postinumeroKoodiUri = Some("posti_04230#2"))

  val Yhteystieto1 = Yhteyshenkilo(
    nimi = Map(Kieli.Fi -> "Aku Ankka", Kieli.Sv -> "Aku Ankka"),
    puhelinnumero = Map(Kieli.Fi -> "123", Kieli.Sv -> "123"),
    sahkoposti = Map(Kieli.Fi -> "aku.ankka@ankkalinnankoulu.fi", Kieli.Sv -> "aku.ankka@ankkalinnankoulu.fi"),
    titteli = Map(Kieli.Fi -> "titteli", Kieli.Sv -> "titteli sv"),
    wwwSivu = Map(Kieli.Fi -> "http://opintopolku.fi", Kieli.Sv -> "http://studieinfo.fi"))

  val JulkaistuHaku = Haku(
    nimi = Map(Kieli.Fi -> "Haku fi", Kieli.Sv -> "Haku sv"),
    tila = Julkaisutila.Julkaistu,
    hakutapaKoodiUri = Some("hakutapa_03#1"),
    hakukohteenLiittamisenTakaraja = Some(inFuture()),
    hakukohteenMuokkaamisenTakaraja = Some(inFuture()),
    ajastettuJulkaisu = Some(inFuture()),
    alkamiskausiKoodiUri = Some("kausi_k#1"),
    alkamisvuosi = Some(LocalDate.now().getYear.toString),
    kohdejoukkoKoodiUri = Some("haunkohdejoukko_17#1"),
    kohdejoukonTarkenneKoodiUri = Some("haunkohdejoukontarkenne_1#1"),
    hakulomaketyyppi = Some(Hakulomaketyyppi.EiSähköistä),
    hakulomakeAtaruId = Some(UUID.randomUUID()),
    hakulomakeKuvaus = Map(Kieli.Fi -> "Hakulomake tulostetaan ja toimitetaan postitse", Kieli.Sv -> "Hakulomake tulostetaan ja toimitetaan postitse sv"),
    hakulomakeLinkki = Map(Kieli.Fi -> "https://koulu.test/hakemusinfo-fi", Kieli.Sv -> "https://koulu.test/hakemusinfo-sv"),
    metadata = Some(HakuMetadata(Seq(Yhteystieto1), Seq(Ajanjakso(alkaa = now(), paattyy = inFuture())))),
    hakuajat = List(Ajanjakso(alkaa = now(), paattyy = inFuture())),
    organisaatioOid = ChildOid,
    muokkaaja = TestUserOid,
    kielivalinta = Seq(Kieli.Fi, Kieli.Sv),
    modified = None)

  val MinHaku = Haku(
    muokkaaja = TestUserOid,
    organisaatioOid = LonelyOid,
    kielivalinta = Seq(Kieli.Fi, Kieli.Sv),
    nimi = kieliMap("Minimi haku"),
    modified = None)
}
