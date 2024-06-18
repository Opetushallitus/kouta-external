package fi.oph.kouta.external

import fi.oph.kouta.domain.oid.OrganisaatioOid
import fi.oph.kouta.domain.{Alkamiskausityyppi, Kieli, Koulutustyyppi}
import fi.oph.kouta.external.domain.indexed.KoodiUri
import fi.oph.kouta.external.swagger.SwaggerModel

import java.time.LocalDateTime
import java.util.UUID

package object domain {

  @SwaggerModel(
    """    Hakutermi:
      |      type: string
      |      enum:
      |        - hakeutuminen
      |        - ilmoittautuminen
      |""")
  abstract class HakutermiSwagger

  @SwaggerModel(
  """
    |    Koulutustyyppi:
    |      type: string
    |      enum:
    |        - aikuisten-perusopetus
    |        - amk
    |        - amm
    |        - amm-muu
    |        - amm-ope-erityisope-ja-opo,
    |        - amm-osaamisala
    |        - amm-tutkinnon-osa
    |        - kk-opintojakso
    |        - lk
    |        - muu
    |        - telma
    |        - tuva
    |        - vapaa-sivistystyo-muu
    |        - vapaa-sivistystyo-opistovuosi
    |        - yo
""")
  abstract class KoulutustyyppiSwagger

  // Kielen swaggeri on tässä, koska Kieli-luokka on kouta-commonissa
  @SwaggerModel(
    """    Kieli:
      |      type: string
      |      enum:
      |        - fi
      |        - sv
      |        - en
      |""")
  abstract class KieliSwagger

  // Kielistetyn swaggerit ovat tässä, koska pelkälle typelle ei voi asettaa annotaatiota
  @SwaggerModel(
    """    Teksti:
      |      type: object
      |      properties:
      |        fi:
      |          type: string
      |          example: Suomenkielinen teksti
      |          description: "Suomenkielinen teksti, jos kielivalinnassa on 'fi'"
      |        sv:
      |          type: string
      |          example: Ruotsinkielinen teksti
      |          description: "Ruotsinkielinen teksti, jos kielivalinnassa on 'sv'"
      |        en:
      |          type: string
      |          example: Englanninkielinen teksti
      |          description: "Englanninkielinen teksti, jos kielivalinnassa on 'en'"
      |    Nimi:
      |      type: object
      |      properties:
      |        fi:
      |          type: string
      |          example: Suomenkielinen nimi
      |          description: "Suomenkielinen nimi, jos kielivalinnassa on 'fi'"
      |        sv:
      |          type: string
      |          example: Ruotsinkielinen nimi
      |          description: "Ruotsinkielinen nimi, jos kielivalinnassa on 'sv'"
      |        en:
      |          type: string
      |          example: Englanninkielinen nimi
      |          description: "Englanninkielinen nimi, jos kielivalinnassa on 'en'"
      |    Kuvaus:
      |      type: object
      |      properties:
      |        fi:
      |          type: string
      |          example: Suomenkielinen kuvaus
      |          description: "Suomenkielinen kuvaus, jos kielivalinnassa on 'fi'"
      |        sv:
      |          type: string
      |          example: Ruotsinkielinen kuvaus
      |          description: "Ruotsinkielinen kuvaus, jos kielivalinnassa on 'sv'"
      |        en:
      |          type: string
      |          example: Englanninkielinen kuvaus
      |          description: "Englanninkielinen kuvaus, jos kielivalinnassa on 'en'"
      |    Linkki:
      |      type: object
      |      properties:
      |        fi:
      |          type: string
      |          example: Linkki suomenkieliselle sivulle
      |          description: "Linkki suomenkieliselle sivulle, jos kielivalinnassa on 'fi'"
      |        sv:
      |          type: string
      |          example: Linkki ruotsinkieliselle sivulle
      |          description: "Linkki ruotsinkieliselle sivulle, jos kielivalinnassa on 'sv'"
      |        en:
      |          type: string
      |          example: Linkki englanninkieliselle sivulle
      |          description: "Linkki englanninkieliselle sivulle, jos kielivalinnassa on 'en'"
      |""")
  abstract class KielistettySwagger

  type Kielistetty = Map[Kieli, String]

  type KielistettyPostinumero = Map[Kieli, Postinumerokoodi]

  @SwaggerModel(
    """    Lisatieto:
      |      type: object
      |      properties:
      |        otsikkoKoodiUri:
      |          type: string
      |          description: Lisätiedon otsikon koodi URI. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/koulutuksenlisatiedot/1)
      |          example: koulutuksenlisatiedot_03#1
      |        teksti:
      |          type: object
      |          description: Lisätiedon teksti eri kielillä. Kielet on määritetty kielivalinnassa.
      |          $ref: '#/components/schemas/Teksti'
      |""")
  case class Lisatieto(otsikkoKoodiUri: String, teksti: Kielistetty)

  @SwaggerModel(
    """    Yhteyshenkilo:
      |      type: object
      |      properties:
      |        nimi:
      |          type: object
      |          description: Yhteyshenkilön nimi eri kielillä. Kielet on määritetty kielivalinnassa.
      |          $ref: '#/components/schemas/Teksti'
      |        titteli:
      |          type: object
      |          description: Yhteyshenkilön titteli eri kielillä. Kielet on määritetty kielivalinnassa.
      |          $ref: '#/components/schemas/Teksti'
      |        sahkoposti:
      |          type: object
      |          description: Yhteyshenkilön sähköpostiosoite eri kielillä. Kielet on määritetty kielivalinnassa.
      |          $ref: '#/components/schemas/Teksti'
      |        puhelinnumero:
      |          type: object
      |          description: Yhteyshenkilön puhelinnumero eri kielillä. Kielet on määritetty kielivalinnassa.
      |          $ref: '#/components/schemas/Teksti'
      |        wwwSivu:
      |          type: object
      |          description: Yhteyshenkilön www-sivun linkki eri kielillä. Kielet on määritetty kielivalinnassa.
      |          $ref: '#/components/schemas/Teksti'
      |        wwwSivuTeksti:
      |          type: object
      |          description: Yhteyshenkilön www-sivun linkin kanssa näytettävä teksti eri kielillä. Kielet on määritetty kielivalinnassa.
      |          $ref: '#/components/schemas/Teksti'
      |""")
  case class Yhteyshenkilo(
      nimi: Kielistetty,
      titteli: Kielistetty,
      sahkoposti: Kielistetty,
      puhelinnumero: Kielistetty,
      wwwSivu: Kielistetty,
      wwwSivuTeksti: Option[Kielistetty]
  )

  @SwaggerModel(
    """    Ajanjakso:
      |      type: object
      |      properties:
      |        alkaa:
      |           type: string
      |           format: date-time
      |           description: Ajanjakson alkuaika
      |           example: 2019-08-23T09:55
      |        paattyy:
      |           type: string
      |           format: date-time
      |           description: Ajanjakson päättymisaika
      |           example: 2019-08-23T09:55
      |""")
  case class Ajanjakso(alkaa: LocalDateTime, paattyy: Option[LocalDateTime])

  @SwaggerModel(
    """    Valintakoe:
      |      type: object
      |      description: Valintakokeen tiedot
      |      properties:
      |        id:
      |          type: string
      |          description: Valintakokeen yksilöivä tunniste. Järjestelmän generoima.
      |          example: "ea596a9c-5940-497e-b5b7-aded3a2352a7"
      |        tyyppiKoodiUri:
      |          type: string
      |          description: Valintakokeen tyyppi. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/valintakokeentyyppi/1)
      |          example: valintakokeentyyppi_1#1
      |        nimi:
      |          type: object
      |          description: Valintakokeen Opintopolussa näytettävä nimi eri kielillä. Kielet on määritetty kielivalinnassa.
      |          $ref: '#/components/schemas/Nimi'
      |        metadata:
      |          type: object
      |          $ref: '#/components/schemas/ValintakoeMetadata'
      |        tilaisuudet:
      |          type: array
      |          description: Valintakokeen järjestämistilaisuudet
      |          items:
      |            $ref: '#/components/schemas/Valintakoetilaisuus'
      |""")
  case class Valintakoe(
      id: Option[UUID] = None,
      tyyppiKoodiUri: Option[String] = None,
      nimi: Kielistetty = Map(),
      metadata: Option[ValintaKoeMetadata],
      tilaisuudet: List[Valintakoetilaisuus] = List()
  )

  @SwaggerModel(
    """    ValintakoeMetadata:
      |      type: object
      |      properties:
      |        tietoja:
      |          type: object
      |          description: Tietoa valintakokeesta
      |          $ref: '#/components/schemas/Teksti'
      |        vahimmaispisteet:
      |          type: double
      |          description: Valintakokeen vähimmäispisteet
      |          example: 10.0
      |        liittyyEnnakkovalmistautumista:
      |          type: boolean
      |          description: Liittyykö valintakokeeseen ennakkovalmistautumista
      |        ohjeetEnnakkovalmistautumiseen:
      |          type: object
      |          description: Ohjeet valintakokeen ennakkojärjestelyihin
      |          $ref: '#/components/schemas/Teksti'
      |        erityisjarjestelytMahdollisia:
      |          type: boolean
      |          description: Ovatko erityisjärjestelyt mahdollisia valintakokeessa
      |        ohjeetErityisjarjestelyihin:
      |          type: object
      |          description: Ohjeet valintakokeen erityisjärjestelyihin
      |          $ref: '#/components/schemas/Teksti'
      |""")
  case class ValintaKoeMetadata(tietoja: Kielistetty = Map(),
                                vahimmaispisteet: Option[Double] = None,
                                liittyyEnnakkovalmistautumista: Option[Boolean] = None,
                                ohjeetEnnakkovalmistautumiseen: Kielistetty = Map(),
                                erityisjarjestelytMahdollisia: Option[Boolean] = None,
                                ohjeetErityisjarjestelyihin: Kielistetty = Map())

  @SwaggerModel(
    """    Valintakoetilaisuus:
      |      type: object
      |      properties:
      |        osoite:
      |          type: object
      |          description: Valintakokeen järjestämispaikan osoite
      |          $ref: '#/components/schemas/Osoite'
      |        aika:
      |          type: array
      |          description: Valintakokeen järjestämisaika
      |          items:
      |            $ref: '#/components/schemas/Ajanjakso'
      |        lisatietoja:
      |          type: object
      |          description: Lisätietoja valintakokeesta eri kielillä. Kielet on määritetty kielivalinnassa.
      |          $ref: '#/components/schemas/Teksti'
      |        jarjestamispaikka:
      |          type: object
      |          description: Valintakokeen järjestämispaikka eri kielillä. Kielet on määritetty kielivalinnassa.
      |          $ref: '#/components/schemas/Teksti'
      |""")
  case class Valintakoetilaisuus(osoite: Option[Osoite],
                                 aika: Option[Ajanjakso],
                                 lisatietoja: Kielistetty,
                                 jarjestamispaikka: Kielistetty = Map())

  @SwaggerModel(
    """    ValintakokeenLisatilaisuudet:
      |      type: object
      |      description: Hakukohteella lisätyt valintakokeen lisätilaisuudet
      |      properties:
      |        id:
      |          type: string
      |          description: Valintakokeen yksilöivä tunniste. Järjestelmän generoima.
      |          example: "ea596a9c-5940-497e-b5b7-aded3a2352a7"
      |        tilaisuudet:
      |          type: array
      |          description: Hakukohteella syötetyt valintaperusteen valintakokeen lisäjärjestämistilaisuudet
      |          items:
      |            $ref: '#/components/schemas/Valintakoetilaisuus'
      |""")
  case class ValintakokeenLisatilaisuudet(id: Option[UUID] = None,
                                          tilaisuudet: Seq[Valintakoetilaisuus] = Seq())


  @SwaggerModel(
    """    Osoite:
      |      type: object
      |      properties:
      |        osoite:
      |          type: object
      |          description: Osoite eri kielillä. Kielet on määritetty kielivalinnassa.
      |          $ref: '#/components/schemas/Teksti'
      |        postinumeroKoodiUri:
      |          type: string
      |          description: Postinumero. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/posti/2)
      |          example: "posti_04230#2"
      |""")
  case class Osoite(osoite: Kielistetty, postinumeroKoodiUri: Kielistetty)

  @SwaggerModel(
    """    Ammattinimike:
      |      type: object
      |      properties:
      |        kieli:
      |          type: string
      |          description: Ammattinimikkeen kieli
      |          $ref: '#/components/schemas/Kieli'
      |          example: fi
      |        arvo:
      |          type: string
      |          description: Ammattinimike annetulla kielellä
      |          example: insinööri
      |    Asiasana:
      |      type: object
      |      properties:
      |        kieli:
      |          type: string
      |          description: Asiasanan kieli
      |          $ref: '#/components/schemas/Kieli'
      |          example: fi
      |        arvo:
      |          type: string
      |          description: Asiasana annetulla kielellä
      |          example: robotiikka
      |""")
  case class Keyword(kieli: Kieli, arvo: String)

  @SwaggerModel(
    """    KoulutuksenAlkamiskausi:
      |      type: object
      |      properties:
      |        alkamiskausityyppi:
      |          type: string
      |          description: Alkamiskauden tyyppi
      |          enum:
      |            - 'henkilokohtainen suunnitelma'
      |            - 'tarkka alkamisajankohta'
      |            - 'alkamiskausi ja -vuosi'
      |        koulutuksenAlkamispaivamaara:
      |          type: string
      |          description: Koulutuksen tarkka alkamisen päivämäärä
      |          example: 2019-11-20T12:00
      |        koulutuksenPaattymispaivamaara:
      |          type: string
      |          description: Koulutuksen päättymisen päivämäärä
      |          example: 2019-11-20T12:00
      |        koulutuksenAlkamiskausiKoodiUri:
      |          type: string
      |          description: Koulutusten alkamiskausi. Hakukohteella voi olla eri alkamiskausi kuin haulla.
      |            Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/kausi/1)
      |          example: kausi_k#1
      |        koulutuksenAlkamisvuosi:
      |          type: string
      |          description: Haun koulutusten alkamisvuosi. Hakukohteella voi olla eri alkamisvuosi kuin haulla.
      |          example: 2020
      |        henkilokohtaisenSuunnitelmanLisatiedot:
      |          type: object
      |          description: Lisätietoa koulutuksen alkamisesta henkilökohtaisen suunnitelman mukaan eri kielillä. Kielet on määritetty haun kielivalinnassa.
      |          $ref: '#/components/schemas/Teksti'
      |""")
  case class KoulutuksenAlkamiskausi(alkamiskausityyppi: Option[Alkamiskausityyppi] = None,
                                     henkilokohtaisenSuunnitelmanLisatiedot: Kielistetty = Map(),
                                     koulutuksenAlkamispaivamaara: Option[LocalDateTime] = None,
                                     koulutuksenPaattymispaivamaara: Option[LocalDateTime] = None,
                                     koulutuksenAlkamiskausiKoodiUri: Option[String] = None,
                                     koulutuksenAlkamisvuosi: Option[String] = None)

  @SwaggerModel(
    """    PaateltyAlkamiskausi:
      |      type: object
      |      properties:
      |        alkamiskausityyppi:
      |          type: string
      |          description: Alkamiskauden tyyppi
      |          enum:
      |            - 'henkilokohtainen suunnitelma'
      |            - 'tarkka alkamisajankohta'
      |            - 'alkamiskausi ja -vuosi'
      |        kausiUri:
      |          type: string
      |          description: Päätelty koulutuksen alkamiskausi-koodiUri
      |            Viittaa koodistoon [kausi](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/kausi)
      |          example: kausi_k#1
      |        vuosi:
      |          type: string
      |          description: Päätelty koulutuksen alkamisvuosi
      |          example: 2020
      |""")
  case class PaateltyAlkamiskausi(alkamiskausityyppi: Option[Alkamiskausityyppi] = None,
                                  kausiUri: Option[String] = None,
                                  vuosi: Option[String] = None)

  @SwaggerModel(
    """    TutkinnonOsa:
      |      type: object
      |      properties:
      |        ePerusteId:
      |          type: number
      |          description: Tutkinnon osan käyttämän ePerusteen id.
      |          example: 4804100
      |        koulutusKoodiUri:
      |          type: string
      |          description: Koulutuksen koodi URI. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/koulutus/11)
      |          example: koulutus_371101#1
      |        tutkinnonosaId:
      |          type: number
      |          description: Tutkinnon osan id ePerusteissa
      |          example: 12345
      |        tutkinnonosaViite:
      |          type: number
      |          description: Tutkinnon osan viite
      |          example: 2449201
      |""")
  case class TutkinnonOsa(ePerusteId: Option[Long] = None,
                          koulutusKoodiUri: Option[String] = None,
                          tutkinnonosaId: Option[Long] = None,
                          tutkinnonosaViite: Option[Long] = None)
  @SwaggerModel(
    """    Kielivalikoima:
      |      type: object
      |      properties:
      |        A1Kielet:
      |          type: array
      |          description: Lista koulutuksen toteutuksen A1 kielistä. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/kieli/1)
      |          items:
      |            type: string
      |            example:
      |              - kieli_EN#1
      |              - kieli_FI#1
      |        A2Kielet:
      |          type: array
      |          description: Lista koulutuksen toteutuksen A2 kielistä. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/kieli/1)
      |          items:
      |            type: string
      |            example:
      |              - kieli_EN#1
      |              - kieli_FI#1
      |        B1Kielet:
      |          type: array
      |          description: Lista koulutuksen toteutuksen B1 kielistä. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/kieli/1)
      |          items:
      |            type: string
      |            example:
      |              - kieli_EN#1
      |              - kieli_FI#1
      |        B2Kielet:
      |          type: array
      |          description: Lista koulutuksen toteutuksen B2 kielistä. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/kieli/1)
      |          items:
      |            type: string
      |            example:
      |              - kieli_EN#1
      |              - kieli_FI#1
      |        B3Kielet:
      |          type: array
      |          description: Lista koulutuksen toteutuksen B3 kielistä. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/kieli/1)
      |          items:
      |            type: string
      |            example:
      |              - kieli_EN#1
      |              - kieli_FI#1
      |        aidinkielet:
      |          type: array
      |          description: Lista koulutuksen toteutuksen äidinkielistä. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/kieli/1)
      |          items:
      |            type: string
      |            example:
      |              - kieli_EN#1
      |              - kieli_FI#1
      |        muutKielet:
      |          type: array
      |          description: Lista koulutuksen toteutuksen muista kielistä. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/kieli/1)
      |          items:
      |            type: string
      |            example:
      |              - kieli_EN#1
      |              - kieli_FI#1
      |""")
  case class Kielivalikoima(A1Kielet: Seq[String] = Seq(),
                            A2Kielet: Seq[String] = Seq(),
                            B1Kielet: Seq[String] = Seq(),
                            B2Kielet: Seq[String] = Seq(),
                            B3Kielet: Seq[String] = Seq(),
                            aidinkielet: Seq[String] = Seq(),
                            muutKielet: Seq[String] = Seq())

  @SwaggerModel(
    """    Aloituspaikat:
      |      type: object
      |      properties:
      |        lukumaara:
      |          type: integer
      |          description: Hakukohteen aloituspaikkojen lukumäärä
      |          example: 100
      |        ensikertalaisille:
      |          type: integer
      |          description: Hakukohteen ensikertalaisten aloituspaikkojen lukumäärä
      |          example: 50
      |        kuvaus:
      |          type: object
      |          description: Tarkempi kuvaus aloituspaikoista
      |          $ref: '#/components/schemas/Kuvaus'
      |""")
  case class Aloituspaikat(lukumaara: Option[Int] = None,
                           ensikertalaisille: Option[Int] = None,
                           kuvaus: Kielistetty = Map())

  @SwaggerModel(
    """    Koodi:
      |      type: object
      |      properties:
      |        koodiUri:
      |          type: string
      |          description: Koodin uri
      |          example: koulutus_851401#1
      |        nimi:
      |          type: object
      |          description: Koodin nimi
      |          $ref: '#/components/schemas/KoodiNimi'
      |""")
  case class Koodi(koodiUri: Option[String] = None,
                   nimi: Option[KoodiNimi] = None)

  case class Postinumerokoodi(koodiUri: String,
                              nimi: String)

  @SwaggerModel(
    """    KoodiNimi:
      |      type: object
      |      properties:
      |        fi:
      |          type: string
      |          description: Koodin suomenkielinen nimi
      |          example: Tekn. lis., kemian tekniikka
      |        sv:
      |          type: string
      |          description: Koodin ruotsinkielinen nimi
      |          example: Tekn. lic., kemisk teknik
      |        en:
      |          type: string
      |          description: Koodin englanninkielinen nimi
      |          example: LicSc, Chemical Eng.
      |""")
  case class KoodiNimi(fi: Option[String] = None,
                       sv: Option[String] = None,
                       en: Option[String] = None)


  @SwaggerModel(
    """    Korkeakoulutustyyppi:
      |      type: object
      |      properties:
      |        koulutustyyppi:
      |          type: string
      |          description: "Korkeakoulutustyyppi, sallitut arvot:
      |            'yo' (yliopisto),
      |            'amk' (ammattikorkea)"
      |          example: yo
      |        tarjoajat:
      |          type: array
      |          description: Ko. korkeakoulutustyyppiä tarjoavien organisaatioiden yksilöivät organisaatio-oidit.
      |          items:
      |            type: string
      |          example:
      |            - 1.2.246.562.10.00101010101
      |            - 1.2.246.562.10.00101010102
      |""")
  case class Korkeakoulutustyyppi(koulutustyyppi: Koulutustyyppi, tarjoajat: Seq[OrganisaatioOid])

}

