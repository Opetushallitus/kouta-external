package fi.oph.kouta.external

import fi.oph.kouta.TestOids._
import fi.oph.kouta.domain.oid.KoulutusOid
import fi.oph.kouta.domain.{AlkamiskausiJaVuosi, Amm, EiSähköistä, Euro, Fi, Julkaistu, Kieli, Maksullinen, Sv}
import fi.oph.kouta.external.domain.{Ajanjakso, AmmatillinenKoulutusMetadata, AmmatillinenOsaamisala, AmmatillinenToteutusMetadata, Apuraha, Haku, HakuMetadata, Keyword, KoulutuksenAlkamiskausi, Koulutus, Lisatieto, Opetus, Osoite, Toteutus, Yhteyshenkilo}

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

object TestData {

  type Kielistetty = Map[Kieli, String]

  def now() = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)

  def inFuture(s: Long = 500) = LocalDateTime.now().plusSeconds(s).truncatedTo(ChronoUnit.MINUTES)

  def inPast(s: Long = 500) = LocalDateTime.now().minusSeconds(s).truncatedTo(ChronoUnit.MINUTES)

  def kieliMap(text: String): Kielistetty = Map(Fi -> s"$text fi", Sv -> s"$text sv")

  def getInvalidHakuajat = List(Ajanjakso(TestData.inFuture(9000), Some(TestData.inFuture(3000))))

  val Osoite1 = Osoite(
    osoite = Map(Fi -> "Kivatie 1", Sv -> "kivavägen 1"),
    postinumeroKoodiUri = Some("posti_04230#2"))

  val Yhteystieto1 = Yhteyshenkilo(
    nimi = Map(Fi -> "Aku Ankka", Sv -> "Aku Ankka"),
    puhelinnumero = Map(Fi -> "123", Sv -> "123"),
    sahkoposti = Map(Fi -> "aku.ankka@ankkalinnankoulu.fi", Sv -> "aku.ankka@ankkalinnankoulu.fi"),
    titteli = Map(Fi -> "titteli", Sv -> "titteli sv"),
    wwwSivu = Map(Fi -> "http://opintopolku.fi", Sv -> "http://studieinfo.fi"))

  val ToteutuksenOpetus: Opetus = Opetus(
    opetuskieliKoodiUrit = Seq("oppilaitoksenopetuskieli_1#1"),
    opetuskieletKuvaus = Map(Fi -> "Kielikuvaus fi", Sv -> "Kielikuvaus sv"),
    opetusaikaKoodiUrit = Seq("opetusaikakk_1#1"),
    opetusaikaKuvaus = Map(Fi -> "Opetusaikakuvaus fi", Sv -> "Opetusaikakuvaus sv"),
    opetustapaKoodiUrit = Seq("opetuspaikkakk_1#1", "opetuspaikkakk_2#1"),
    opetustapaKuvaus = Map(Fi -> "Opetustapakuvaus fi", Sv -> "Opetustapakuvaus sv"),
    maksullisuustyyppi = Some(Maksullinen),
    maksullisuusKuvaus = Map(Fi -> "Maksullisuuskuvaus fi", Sv -> "Maksullisuuskuvaus sv"),
    maksunMaara = Some(200.5),
    koulutuksenAlkamiskausi = Some(KoulutuksenAlkamiskausi(
      alkamiskausityyppi = Some(AlkamiskausiJaVuosi),
      henkilokohtaisenSuunnitelmanLisatiedot = Map(Fi -> "Jotakin lisätietoa", Sv -> "Jotakin lisätietoa sv"),
      koulutuksenAlkamispaivamaara = None,
      koulutuksenPaattymispaivamaara = None,
      koulutuksenAlkamiskausiKoodiUri = Some("kausi_k#1"),
      koulutuksenAlkamisvuosi = Some(LocalDate.now().getYear.toString))),
    lisatiedot = Seq(
      Lisatieto(otsikkoKoodiUri = "koulutuksenlisatiedot_03#1",
        teksti = Map(Fi -> "Opintojen rakenteen kuvaus", Sv -> "Rakenne kuvaus sv")),
      Lisatieto(otsikkoKoodiUri = "koulutuksenlisatiedot_03#1",
        teksti = Map(Fi -> "Sisältö kuvaus", Sv -> "Sisältö kuvaus sv"))),
    onkoApuraha = true,
    apuraha = Some(Apuraha(
      min = Some(100),
      max = Some(200),
      yksikko = Some(Euro),
      kuvaus = Map(Fi -> "apurahakuvaus fi", Sv -> "apurahakuvaus sv"),
    )),
    suunniteltuKestoVuodet = Some(3),
    suunniteltuKestoKuukaudet = Some(10),
    suunniteltuKestoKuvaus = Map(Fi -> "Keston kuvaus fi", Sv -> "Keston kuvaus sv")
  )

  val JulkaistuHaku = Haku(
    externalId = Some("ext1"),
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
    metadata = Some(HakuMetadata(
      Seq(Yhteystieto1),
      Seq(Ajanjakso(alkaa = now(), paattyy = Some(inFuture()))),
      koulutuksenAlkamiskausi = Some(KoulutuksenAlkamiskausi(
        alkamiskausityyppi = Some(AlkamiskausiJaVuosi),
        henkilokohtaisenSuunnitelmanLisatiedot = Map(Fi -> "Jotakin lisätietoa", Sv -> "Jotakin lisätietoa sv"),
        koulutuksenAlkamispaivamaara = None,
        koulutuksenPaattymispaivamaara = None,
        koulutuksenAlkamiskausiKoodiUri = Some("kausi_k#1"),
        koulutuksenAlkamisvuosi = Some(LocalDate.now().getYear.toString))))),
    hakuajat = List(Ajanjakso(alkaa = now(), paattyy = Some(inFuture()))),
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

  val AmmKoulutus = Koulutus(
    oid = None,
    externalId = Some("extKoulutus1"),
    johtaaTutkintoon = true,
    koulutustyyppi = Amm,
    koulutuksetKoodiUri = Seq("koulutus_371101#1"),
    tila = Julkaistu,
    tarjoajat = List(GrandChildOid, EvilGrandChildOid, EvilCousin),
    nimi = Map(Fi -> "Koulutus fi", Sv -> "Koulutus sv"),
    sorakuvausId = Some(UUID.randomUUID()),
    metadata = Some(AmmatillinenKoulutusMetadata(
      kuvaus = Map(Fi -> "kuvaus", Sv -> "kuvaus sv"),
      lisatiedot = Seq(
        Lisatieto(otsikkoKoodiUri = "koulutuksenlisatiedot_03#1",
          teksti = Map(Fi -> "Opintojen lisätieto ", Sv -> "Opintojen lisätieto sv"))))),
    julkinen = true,
    muokkaaja = TestUserOid,
    organisaatioOid = ChildOid,
    kielivalinta = Seq(Fi, Sv),
    teemakuva = None,
    ePerusteId = Some(1234567),
    modified = None
  )

  val AmmToteutus = Toteutus(
    oid = None,
    koulutusOid = KoulutusOid("1.2.246.562.13.123456789"),
    externalId = Some("extToteutus1"),
    tila = Julkaistu,
    tarjoajat = List(GrandChildOid, EvilGrandChildOid, EvilCousin),
    nimi = Map(Fi -> "Toteutus fi", Sv -> "Toteutus sv"),
    metadata = Some(AmmatillinenToteutusMetadata(
      kuvaus = Map(Fi -> "kuvaus", Sv -> "kuvaus sv"),
      osaamisalat = List(AmmatillinenOsaamisala("osaamisala_0001#1",
        linkki = Map(Fi -> "http://osaamisala.fi/linkki/fi", Sv -> "http://osaamisala.fi/linkki/sv"),
        otsikko = Map(Fi -> "Katso osaamisalan tarkempi kuvaus tästä", Sv -> "Katso osaamisalan tarkempi kuvaus tästä sv"))),
      opetus = Some(ToteutuksenOpetus),
      asiasanat = List(Keyword(Fi, "robotiikka"), Keyword(Fi, "robottiautomatiikka")),
      ammattinimikkeet = List(Keyword(Fi, "insinööri"), Keyword(Fi, "koneinsinööri")),
      yhteyshenkilot = Seq(Yhteystieto1),
      ammatillinenPerustutkintoErityisopetuksena = false
    )),
    sorakuvausId = None,
    muokkaaja = TestUserOid,
    organisaatioOid = ChildOid,
    kielivalinta = Seq(Fi, Sv),
    teemakuva = None,
    modified = None
  )
}
