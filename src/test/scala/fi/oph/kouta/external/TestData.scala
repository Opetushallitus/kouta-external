package fi.oph.kouta.external

import fi.oph.kouta.TestOids._
import fi.oph.kouta.domain.oid.{HakuOid, KoulutusOid, ToteutusOid}
import fi.oph.kouta.domain.{
  AlkamiskausiJaVuosi,
  Amm,
  EiSähköistä,
  Euro,
  Fi,
  Hakijapalvelu,
  Julkaistu,
  Kieli,
  Maksullinen,
  MuuOsoite,
  Sv,
  Tallennettu,
  TarkkaAlkamisajankohta
}
import fi.oph.kouta.external.domain.{
  Ajanjakso,
  Aloituspaikat,
  AmmatillinenKoulutusMetadata,
  AmmatillinenOsaamisala,
  AmmatillinenToteutusMetadata,
  Apuraha,
  Column,
  GenericValintaperusteMetadata,
  Haku,
  HakuMetadata,
  Hakukohde,
  HakukohdeMetadata,
  Keyword,
  KoulutuksenAlkamiskausi,
  Koulutus,
  Liite,
  LiitteenToimitusosoite,
  Lisatieto,
  Opetus,
  Osoite,
  Row,
  SisaltoTeksti,
  Sorakuvaus,
  SorakuvausMetadata,
  Taulukko,
  Toteutus,
  ValintaKoeMetadata,
  Valintakoe,
  Valintakoetilaisuus,
  ValintakokeenLisatilaisuudet,
  Valintaperuste,
  Valintatapa,
  Yhteyshenkilo
}

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime, LocalTime}
import java.util.UUID

object TestData {

  type Kielistetty = Map[Kieli, String]

  def now() = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)

  def inFuture(s: Long = 500) = LocalDateTime.now().plusSeconds(s).truncatedTo(ChronoUnit.MINUTES)

  def inPast(s: Long = 500) = LocalDateTime.now().minusSeconds(s).truncatedTo(ChronoUnit.MINUTES)

  def kieliMap(text: String): Kielistetty = Map(Fi -> s"$text fi", Sv -> s"$text sv")

  def getInvalidHakuajat = List(Ajanjakso(TestData.inFuture(9000), Some(TestData.inFuture(3000))))

  val startTime1: LocalDateTime =
    LocalDate.now().plusDays(1).atTime(LocalTime.parse("09:49")).truncatedTo(ChronoUnit.MINUTES)
  val endTime1: LocalDateTime =
    LocalDate.now().plusDays(1).atTime(LocalTime.parse("09:58")).truncatedTo(ChronoUnit.MINUTES)

  val Osoite1 =
    Osoite(osoite = Map(Fi -> "Kivatie 1", Sv -> "kivavägen 1"), postinumeroKoodiUri = Some("posti_04230#2"))

  val Yhteystieto1 = Yhteyshenkilo(
    nimi = Map(Fi -> "Aku Ankka", Sv -> "Aku Ankka"),
    puhelinnumero = Map(Fi -> "123", Sv -> "123"),
    sahkoposti = Map(Fi -> "aku.ankka@ankkalinnankoulu.fi", Sv -> "aku.ankka@ankkalinnankoulu.fi"),
    titteli = Map(Fi -> "titteli", Sv -> "titteli sv"),
    wwwSivu = Map(Fi -> "http://opintopolku.fi", Sv -> "http://studieinfo.fi"),
    wwwSivuTeksti = None
  )

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
    koulutuksenAlkamiskausi = Some(
      KoulutuksenAlkamiskausi(
        alkamiskausityyppi = Some(AlkamiskausiJaVuosi),
        henkilokohtaisenSuunnitelmanLisatiedot = Map(Fi -> "Jotakin lisätietoa", Sv -> "Jotakin lisätietoa sv"),
        koulutuksenAlkamispaivamaara = None,
        koulutuksenPaattymispaivamaara = None,
        koulutuksenAlkamiskausiKoodiUri = Some("kausi_k#1"),
        koulutuksenAlkamisvuosi = Some(LocalDate.now().getYear.toString)
      )
    ),
    lisatiedot = Seq(
      Lisatieto(
        otsikkoKoodiUri = "koulutuksenlisatiedot_03#1",
        teksti = Map(Fi -> "Opintojen rakenteen kuvaus", Sv -> "Rakenne kuvaus sv")
      ),
      Lisatieto(
        otsikkoKoodiUri = "koulutuksenlisatiedot_03#1",
        teksti = Map(Fi -> "Sisältö kuvaus", Sv -> "Sisältö kuvaus sv")
      )
    ),
    onkoApuraha = true,
    apuraha = Some(
      Apuraha(
        min = Some(100),
        max = Some(200),
        yksikko = Some(Euro),
        kuvaus = Map(Fi -> "apurahakuvaus fi", Sv -> "apurahakuvaus sv")
      )
    ),
    suunniteltuKestoVuodet = Some(3),
    suunniteltuKestoKuukaudet = Some(10),
    suunniteltuKestoKuvaus = Map(Fi -> "Keston kuvaus fi", Sv -> "Keston kuvaus sv")
  )

  val Liite1: Liite = Liite(
    id = None,
    tyyppiKoodiUri = Some("liitetyypitamm_2#1"),
    nimi = Map(Fi -> "liite 1 Fi", Sv -> "liite 1 Sv"),
    kuvaus = Map(Fi -> "kuvaus Fi", Sv -> "kuvaus Sv"),
    toimitusaika = Some(inFuture()),
    toimitustapa = Some(Hakijapalvelu),
    toimitusosoite = None
  )

  val Liite2: Liite = Liite(
    id = None,
    tyyppiKoodiUri = Some("liitetyypitamm_1#1"),
    nimi = Map(Fi -> "liite 2 Fi", Sv -> "liite 2 Sv"),
    kuvaus = Map(Fi -> "kuvaus Fi", Sv -> "kuvaus Sv"),
    toimitusaika = None,
    toimitustapa = Some(MuuOsoite),
    toimitusosoite = Some(LiitteenToimitusosoite(osoite = Osoite1, sahkoposti = Some("foo@bar.fi"), verkkosivu = None))
  )

  val ValintakokeenLisatilaisuudet1: ValintakokeenLisatilaisuudet = ValintakokeenLisatilaisuudet(
    id = None,
    tilaisuudet = List(
      Valintakoetilaisuus(
        jarjestamispaikka = Map(Fi -> "Lisä järjestämispaikka fi", Sv -> "Lisä järjestämispaikka sv"),
        osoite = Some(Osoite1),
        aika = Some(Ajanjakso(alkaa = startTime1, paattyy = Some(endTime1))),
        lisatietoja = Map(Fi -> "lisätieto fi", Sv -> "lisätieto sv")
      )
    )
  )

  val Valintakoe1: Valintakoe = Valintakoe(
    id = None,
    tyyppiKoodiUri = Some("valintakokeentyyppi_1#1"),
    nimi = Map(Fi -> "valintakokeen nimi fi", Sv -> "valintakokeen nimi sv"),
    metadata = Some(
      ValintaKoeMetadata(
        tietoja = Map(Fi -> "tietoa valintakokeesta fi", Sv -> "tietoa valintakokeesta sv"),
        vahimmaispisteet = Some(182.1),
        liittyyEnnakkovalmistautumista = Some(true),
        ohjeetEnnakkovalmistautumiseen = Map(Fi -> "Ennakko-ohjeet fi", Sv -> "Ennakko-ohjeet sv"),
        erityisjarjestelytMahdollisia = Some(true),
        ohjeetErityisjarjestelyihin = Map(Fi -> "Erityisvalmistelut fi", Sv -> "Erityisvalmistelut sv")
      )
    ),
    tilaisuudet = List(
      Valintakoetilaisuus(
        jarjestamispaikka = Map(Fi -> "Järjestämispaikka fi", Sv -> "Järjestämispaikka sv"),
        osoite = Some(Osoite1),
        aika = Some(Ajanjakso(alkaa = now(), paattyy = Some(inFuture()))),
        lisatietoja = Map(Fi -> "lisätieto fi", Sv -> "lisätieto sv")
      )
    )
  )

  val Taulukko1: Taulukko = Taulukko(
    id = None,
    nimi = Map(Fi -> "Taulukko 1", Sv -> "Taulukko 1 sv"),
    rows = Seq(
      Row(
        index = 0,
        isHeader = true,
        columns = Seq(
          Column(index = 0, text = Map(Fi -> "Otsikko", Sv -> "Otsikko sv")),
          Column(index = 1, text = Map(Fi -> "Otsikko 2", Sv -> "Otsikko 2 sv"))
        )
      ),
      Row(
        index = 1,
        isHeader = false,
        columns = Seq(
          Column(index = 0, text = Map(Fi -> "Tekstiä", Sv -> "Tekstiä sv")),
          Column(index = 1, text = Map(Fi -> "Tekstiä 2", Sv -> "Tekstiä 2 sv"))
        )
      )
    )
  )

  val Taulukko2: Taulukko = Taulukko(
    id = None,
    nimi = Map(Fi -> "Taulukko 2", Sv -> "Taulukko 2 sv"),
    rows = Seq(
      Row(
        index = 0,
        isHeader = true,
        columns = Seq(
          Column(index = 0, text = Map(Fi -> "Otsikko", Sv -> "Otsikko sv")),
          Column(index = 1, text = Map(Fi -> "Otsikko 2", Sv -> "Otsikko 2 sv"))
        )
      ),
      Row(
        index = 1,
        isHeader = false,
        columns = Seq(
          Column(index = 0, text = Map(Fi -> "Tekstiä", Sv -> "Tekstiä sv")),
          Column(index = 1, text = Map(Fi -> "Tekstiä 2", Sv -> "Tekstiä 2 sv"))
        )
      )
    )
  )

  val Valintatapa1: Valintatapa = Valintatapa(
    nimi = kieliMap("Valintatapa1"),
    valintatapaKoodiUri = Some("valintatapajono_av#1"),
    sisalto = Seq(SisaltoTeksti(kieliMap("Sisaltoteksti")), Taulukko1, Taulukko2),
    kaytaMuuntotaulukkoa = false,
    kynnysehto = kieliMap("kynnysehto"),
    enimmaispisteet = Some(201.15),
    vahimmaispisteet = Some(182.1)
  )

  val Valintatapa2: Valintatapa = Valintatapa(
    nimi = kieliMap("Valintatapa2"),
    valintatapaKoodiUri = Some("valintatapajono_tv#1"),
    sisalto = Seq(SisaltoTeksti(kieliMap("Sisaltoteksti")), Taulukko2),
    kaytaMuuntotaulukkoa = true,
    kynnysehto = kieliMap("kynnysehto"),
    enimmaispisteet = Some(18.1),
    vahimmaispisteet = Some(10.1)
  )

  val JulkaistuHaku = Haku(
    externalId = Some("ext1"),
    hakukohdeOids = None,
    nimi = Map(Fi -> "Haku fi", Sv -> "Haku sv"),
    tila = Julkaistu,
    hakutapaKoodiUri = Some("hakutapa_03#1"),
    hakukohteenLiittamisenTakaraja = Some(inFuture()),
    hakukohteenMuokkaamisenTakaraja = Some(inFuture()),
    ajastettuJulkaisu = Some(inFuture()),
    alkamiskausiKoodiUri = None,
    alkamisvuosi = Some(LocalDate.now().getYear.toString),
    kohdejoukkoKoodiUri = Some("haunkohdejoukko_17#1"),
    kohdejoukonTarkenneKoodiUri = Some("haunkohdejoukontarkenne_1#1"),
    hakulomaketyyppi = Some(EiSähköistä),
    hakulomakeAtaruId = Some(UUID.randomUUID()),
    hakulomakeKuvaus = Map(
      Fi -> "Hakulomake tulostetaan ja toimitetaan postitse",
      Sv -> "Hakulomake tulostetaan ja toimitetaan postitse sv"
    ),
    hakulomakeLinkki = Map(Fi -> "https://koulu.test/hakemusinfo-fi", Sv -> "https://koulu.test/hakemusinfo-sv"),
    hakuvuosi = Some(LocalDate.now().getYear),
    hakukausi = None,
    metadata = Some(
      HakuMetadata(
        Seq(Yhteystieto1),
        Seq(Ajanjakso(alkaa = now(), paattyy = Some(inFuture()))),
        koulutuksenAlkamiskausi = Some(
          KoulutuksenAlkamiskausi(
            alkamiskausityyppi = Some(AlkamiskausiJaVuosi),
            henkilokohtaisenSuunnitelmanLisatiedot = Map(Fi -> "Jotakin lisätietoa", Sv -> "Jotakin lisätietoa sv"),
            koulutuksenAlkamispaivamaara = None,
            koulutuksenPaattymispaivamaara = None,
            koulutuksenAlkamiskausiKoodiUri = Some("kausi_k#1"),
            koulutuksenAlkamisvuosi = Some(LocalDate.now().getYear.toString)
          )
        )
      )
    ),
    hakuajat = List(Ajanjakso(alkaa = now(), paattyy = Some(inFuture()))),
    valintakokeet = Some(List(Valintakoe1)),
    organisaatioOid = ChildOid,
    muokkaaja = TestUserOid,
    kielivalinta = Seq(Fi, Sv),
    modified = None
  )

  val MinHaku = Haku(
    muokkaaja = TestUserOid,
    organisaatioOid = LonelyOid,
    kielivalinta = Seq(Fi, Sv),
    nimi = kieliMap("Minimi haku"),
    modified = None
  )

  val JulkaistuHakukohde = Hakukohde(
    oid = None,
    externalId = Some("extHakukohde"),
    toteutusOid = ToteutusOid("1.2.246.562.17.123"),
    hakuOid = HakuOid("1.2.246.562.29.123"),
    tila = Julkaistu,
    nimi = Map(Fi -> "Hakukohde fi", Sv -> "Hakukohde sv"),
    tarjoaja = Some(OtherOid),
    hakulomaketyyppi = Some(EiSähköistä),
    hakulomakeAtaruId = Some(UUID.randomUUID()),
    hakulomakeKuvaus = Map(
      Fi -> "Hakulomake tulostetaan ja toimitetaan postitse",
      Sv -> "Hakulomake tulostetaan ja toimitetaan postitse sv"
    ),
    hakulomakeLinkki =
      Map(Fi -> "https://koulu.test/kohteen-hakemusinfo-fi", Sv -> "https://koulu.test/kohteen-hakemusinfo-sv"),
    hakukohderyhmat = None,
    kaytetaanHaunHakulomaketta = Some(false),
    pohjakoulutusvaatimusKoodiUrit = Seq("pohjakoulutusvaatimuskouta_pk#1", "pohjakoulutusvaatimuskouta_yo#1"),
    pohjakoulutusvaatimusTarkenne = kieliMap("Pohjakoulutusvaatimuksen tarkenne"),
    muuPohjakoulutusvaatimus = Map(),
    toinenAsteOnkoKaksoistutkinto = None,
    kaytetaanHaunAikataulua = Some(false),
    valintaperusteId = None,
    liitteetOnkoSamaToimitusaika = Some(true),
    liitteetOnkoSamaToimitusosoite = Some(false),
    liitteidenToimitusaika = Some(inFuture()),
    liitteidenToimitustapa = None,
    liitteidenToimitusosoite = None,
    liitteet = List(Liite1, Liite2),
    valintakokeet = List(Valintakoe1),
    hakuajat = List(Ajanjakso(alkaa = now(), paattyy = Some(inFuture()))),
    metadata = Some(
      HakukohdeMetadata(
        valintakokeidenYleiskuvaus = Map(Fi -> "yleiskuvaus fi", Sv -> "yleiskuvaus sv"),
        valintaperusteenValintakokeidenLisatilaisuudet = List(ValintakokeenLisatilaisuudet1),
        kynnysehto = Map(Fi -> "Kynnysehto fi", Sv -> "Kynnysehto sv"),
        kaytetaanHaunAlkamiskautta = Some(false),
        koulutuksenAlkamiskausi = Some(
          KoulutuksenAlkamiskausi(
            alkamiskausityyppi = Some(TarkkaAlkamisajankohta),
            koulutuksenAlkamisvuosi = None,
            koulutuksenAlkamiskausiKoodiUri = None,
            koulutuksenAlkamispaivamaara = Some(inFuture(20000)),
            koulutuksenPaattymispaivamaara = Some(inFuture(30000))
          )
        ),
        aloituspaikat = Some(
          Aloituspaikat(
            lukumaara = Some(100),
            ensikertalaisille = Some(50),
            kuvaus = Map(Fi -> "aloituspaikkojen kuvaus fi", Sv -> "aloituspaikkojen kuvaus sv")
          )
        ),
        uudenOpiskelijanUrl = Map()
      )
    ),
    muokkaaja = TestUserOid,
    organisaatioOid = ChildOid,
    kielivalinta = Seq(Fi, Sv),
    modified = None
  )

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
    metadata = Some(
      AmmatillinenKoulutusMetadata(
        kuvaus = Map(Fi -> "kuvaus", Sv -> "kuvaus sv"),
        lisatiedot = Seq(
          Lisatieto(
            otsikkoKoodiUri = "koulutuksenlisatiedot_03#1",
            teksti = Map(Fi -> "Opintojen lisätieto ", Sv -> "Opintojen lisätieto sv")
          )
        )
      )
    ),
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
    metadata = Some(
      AmmatillinenToteutusMetadata(
        kuvaus = Map(Fi -> "kuvaus", Sv -> "kuvaus sv"),
        osaamisalat = List(
          AmmatillinenOsaamisala(
            "osaamisala_0001#1",
            linkki = Map(Fi -> "http://osaamisala.fi/linkki/fi", Sv -> "http://osaamisala.fi/linkki/sv"),
            otsikko =
              Map(Fi -> "Katso osaamisalan tarkempi kuvaus tästä", Sv -> "Katso osaamisalan tarkempi kuvaus tästä sv")
          )
        ),
        opetus = Some(ToteutuksenOpetus),
        asiasanat = List(Keyword(Fi, "robotiikka"), Keyword(Fi, "robottiautomatiikka")),
        ammattinimikkeet = List(Keyword(Fi, "insinööri"), Keyword(Fi, "koneinsinööri")),
        yhteyshenkilot = Seq(Yhteystieto1),
        ammatillinenPerustutkintoErityisopetuksena = false
      )
    ),
    sorakuvausId = None,
    muokkaaja = TestUserOid,
    organisaatioOid = ChildOid,
    kielivalinta = Seq(Fi, Sv),
    teemakuva = None,
    modified = None
  )

  val AmmSorakuvaus: Sorakuvaus = Sorakuvaus(
    id = None,
    tila = Julkaistu,
    nimi = kieliMap("nimi"),
    koulutustyyppi = Amm,
    kielivalinta = List(Fi, Sv),
    metadata = Some(
      SorakuvausMetadata(
        kuvaus = kieliMap("kuvaus"),
        koulutusKoodiUrit = Seq("koulutus_371101#1"),
        koulutusalaKoodiUri = Some("kansallinenkoulutusluokitus2016koulutusalataso2_054#1")
      )
    ),
    organisaatioOid = OphOid,
    muokkaaja = OphUserOid,
    modified = None
  )

  val AmmValintaperusteMetadata = GenericValintaperusteMetadata(
    tyyppi = Amm,
    valintatavat = Seq(Valintatapa1, Valintatapa2),
    valintakokeidenYleiskuvaus = Map(Fi -> "yleiskuvaus fi", Sv -> "yleiskuvaus sv"),
    kuvaus = Map(Fi -> "kuvaus", Sv -> "kuvaus sv"),
    lisatiedot = Map(Fi -> "lisatiedot", Sv -> "lisatiedot sv"),
    hakukelpoisuus = Map(Fi -> "hakukelpoisuus", Sv -> "hakukelpoisuus sv"),
    sisalto = Seq(SisaltoTeksti(kieliMap("Sisaltoteksti")), Taulukko1, Taulukko2)
  )

  val AmmValintaperuste: Valintaperuste = Valintaperuste(
    koulutustyyppi = Amm,
    externalId = None,
    id = None,
    tila = Julkaistu,
    hakutapaKoodiUri = Some("hakutapa_02#1"),
    kohdejoukkoKoodiUri = Some("haunkohdejoukko_17#1"),
    nimi = Map(Fi -> "nimi", Sv -> "nimi sv"),
    julkinen = false,
    valintakokeet = List(Valintakoe1),
    metadata = Some(AmmValintaperusteMetadata),
    organisaatioOid = ChildOid,
    muokkaaja = TestUserOid,
    kielivalinta = List(Fi, Sv),
    modified = None
  )

  val MinYoValintaperuste: Valintaperuste = Valintaperuste(
    koulutustyyppi = Amm,
    externalId = None,
    id = None,
    tila = Tallennettu,
    hakutapaKoodiUri = None,
    kohdejoukkoKoodiUri = None,
    nimi = Map(Fi -> "nimi", Sv -> "nimi sv"),
    julkinen = false,
    valintakokeet = List(),
    metadata = None,
    organisaatioOid = ChildOid,
    muokkaaja = TestUserOid,
    kielivalinta = List(Fi, Sv),
    modified = None
  )

}
