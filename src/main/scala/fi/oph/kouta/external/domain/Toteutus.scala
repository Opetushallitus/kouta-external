package fi.oph.kouta.external.domain

import fi.oph.kouta.domain.oid.{KoulutusOid, OrganisaatioOid, ToteutusOid, UserOid}
import fi.oph.kouta.domain.{Julkaisutila, Kieli, Modified}
import fi.oph.kouta.external.swagger.SwaggerModel

import java.util.UUID

@SwaggerModel(
  """    Toteutus:
    |      type: object
    |      properties:
    |        oid:
    |          type: string
    |          description: Toteutuksen yksilöivä tunniste. Järjestelmän generoima.
    |          example: "1.2.246.562.17.00000000000000000009"
    |        koulutusOid:
    |          type: string
    |          description: Toteutuksen koulutuksen oid
    |          example: "1.2.246.562.11.00000000000000000008"
    |        externalId:
    |          type: string
    |          description: Ulkoinen tunniste jota voidaan käyttää Kouta lomakkeiden mäppäykseen oppilaitosten omien tietojärjestelmien kanssa
    |        tila:
    |          type: string
    |          example: "julkaistu"
    |          enum:
    |            - julkaistu
    |            - arkistoitu
    |            - tallennettu
    |          description: Toteutuksen julkaisutila. Jos toteutus on julkaistu, se näkyy oppijalle Opintopolussa.
    |        tarjoajat:
    |          type: array
    |          description: Toteutusta tarjoavien organisaatioiden yksilöivät organisaatio-oidit
    |          items:
    |            type: string
    |          example:
    |            - 1.2.246.562.10.00101010101
    |            - 1.2.246.562.10.00101010102
    |        kielivalinta:
    |          type: array
    |          description: Kielet, joille toteutuksen nimi, kuvailutiedot ja muut tekstit on käännetty
    |          items:
    |            $ref: '#/components/schemas/Kieli'
    |          example:
    |            - fi
    |            - sv
    |        nimi:
    |          type: object
    |          description: Toteutuksen Opintopolussa näytettävä nimi eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Nimi'
    |        metadata:
    |          type: object
    |          oneOf:
    |            - $ref: '#/components/schemas/YliopistoToteutusMetadata'
    |            - $ref: '#/components/schemas/AmmatillinenToteutusMetadata'
    |            - $ref: '#/components/schemas/AmmattikorkeaToteutusMetadata'
    |            - $ref: '#/components/schemas/AmmOpeErityisopeJaOpoToteutusMetadata'
    |            - $ref: '#/components/schemas/OpePedagOpinnotToteutusMetadata'
    |            - $ref: '#/components/schemas/AmmatillinenTutkinnonOsaToteutusMetadata'
    |            - $ref: '#/components/schemas/AmmatillinenOsaamisalaToteutusMetadata'
    |            - $ref: '#/components/schemas/AmmatillinenMuuToteutusMetadata'
    |            - $ref: '#/components/schemas/LukioToteutusMetadata'
    |            - $ref: '#/components/schemas/TuvaToteutusMetadata'
    |            - $ref: '#/components/schemas/TelmaToteutusMetadata'
    |            - $ref: '#/components/schemas/VapaaSivistystyoOpistovuosiToteutusMetadata'
    |            - $ref: '#/components/schemas/VapaaSivistystyoMuuMetadata'
    |            - $ref: '#/components/schemas/AikuistenPerusopetusToteutusMetadata'
    |            - $ref: '#/components/schemas/KkOpintojaksoToteutusMetadata'
    |            - $ref: '#/components/schemas/ErikoislaakariToteutusMetadata'
    |            - $ref: '#/components/schemas/KkOpintokokonaisuusToteutusMetadata'
    |          example:
    |            tyyppi: amm
    |            kuvaus:
    |              fi: Suomenkielinen kuvaus
    |              sv: Ruotsinkielinen kuvaus
    |            osaamisalat:
    |              - koodiUri: osaamisala_0001#1
    |                linkki:
    |                  fi: http://osaamisala.fi/linkki/fi
    |                  sv: http://osaamisala.fi/linkki/sv
    |                otsikko:
    |                  fi: Katso osaamisalan tarkempi kuvaus tästä
    |                  sv: Katso osaamisalan tarkempi kuvaus tästä ruotsiksi
    |            opetus:
    |              opetuskieliKoodiUrit:
    |                - oppilaitoksenopetuskieli_1#1
    |              opetuskieletKuvaus:
    |                fi: Opetuskielen suomenkielinen kuvaus
    |                sv: Opetuskielen ruotsinkielinen kuvaus
    |              opetusaikaKoodiUrit:
    |                - opetusaikakk_1#1
    |              opetusaikaKuvaus:
    |                fi: Opetusajan suomenkielinen kuvaus
    |                sv: Opetusajan ruotsinkielinen kuvaus
    |              opetustapaKoodiUrit:
    |                - opetuspaikkakk_1#1
    |                - opetuspaikkakk_2#1
    |              opetustapaKuvaus:
    |                fi: Opetustavan suomenkielinen kuvaus
    |                sv: Opetustavan ruotsinkielinen kuvaus
    |              onkoMaksullinen: true
    |              maksullisuusKuvaus:
    |                fi: Maksullisuuden suomenkielinen kuvaus
    |                sv: Maksullisuuden ruotsinkielinen kuvaus
    |              maksunMaara: 200.50
    |              alkamiskausiKoodiUri: kausi_k#1
    |              alkamisvuosi : 2020
    |              alkamisaikaKuvaus:
    |                fi: Alkamisajan suomenkielinen kuvaus
    |                sv: Alkamisajan ruotsinkielinen kuvaus
    |              lisatiedot:
    |                - otsikkoKoodiUri: koulutuksenlisatiedot_03#1
    |                  teksti:
    |                    fi: Suomenkielinen lisätietoteksti
    |                    sv: Ruotsinkielinen lisätietoteksti
    |              onkoLukuvuosimaksua: true
    |              lukuvuosimaksu:
    |                 fi: 200 lukukaudessa
    |                 sv: 200 på svenska
    |              lukuvuosimaksuKuvaus:
    |                fi: Lukuvuosimaksun suomenkielinen kuvaus
    |                sv: Lukuvuosimaksun ruotsinkielinen kuvaus
    |              onkoStipendia: true
    |              stipendinMaara:
    |                 fi: 200 lukukaudessa
    |                 sv: 200 på svenska
    |              stipendinKuvaus:
    |                fi: Stipendin suomenkielinen kuvaus
    |                sv: Stipendin ruotsinkielinen kuvaus
    |            ammattinimikkeet:
    |              - kieli: fi
    |                arvo: insinööri
    |              - kieli: en
    |                arvo: engineer
    |            asiasanat:
    |              - kieli: fi
    |                arvo: ravintotiede
    |              - kieli: en
    |                arvo: nutrition
    |            yhteyshenkilot:
    |              - nimi:
    |                  fi: Aku Ankka
    |                  sv: Kalle Ankka
    |                titteli:
    |                  fi: Ankka
    |                  sv: Ankka ruotsiksi
    |                sahkoposti:
    |                  fi: aku.ankka@ankkalinnankoulu.fi
    |                  sv: aku.ankka@ankkalinnankoulu.fi
    |                puhelinnumero:
    |                  fi: 123
    |                  sv: 223
    |                wwwSivu:
    |                  fi: http://opintopolku.fi
    |                  sv: http://studieinfo.fi
    |        sorakuvausId:
    |          type: string
    |          description: Toteutukseen liittyvän SORA-kuvauksen yksilöivä tunniste
    |          example: "ea596a9c-5940-497e-b5b7-aded3a2352a7"
    |        muokkaaja:
    |          type: string
    |          description: Toteutusta viimeksi muokanneen virkailijan henkilö-oid
    |          example: 1.2.246.562.10.00101010101
    |        organisaatioOid:
    |           type: string
    |           description: Toteutuksen luoneen organisaation oid
    |           example: 1.2.246.562.10.00101010101
    |        teemakuva:
    |          type: string
    |          description: Toteutuksen Opintopolussa näytettävän teemakuvan URL.
    |          example: https://konfo-files.opintopolku.fi/toteutus-teema/1.2.246.562.13.00000000000000000009/f4ecc80a-f664-40ef-98e6-eaf8dfa57f6e.png
    |        modified:
    |           type: string
    |           format: date-time
    |           description: Toteutuksen viimeisin muokkausaika. Järjestelmän generoima
    |           example: 2019-08-23T09:55
    |"""
)
case class Toteutus(
    oid: Option[ToteutusOid],
    externalId: Option[String],
    koulutusOid: KoulutusOid,
    tila: Julkaisutila,
    tarjoajat: List[OrganisaatioOid],
    nimi: Kielistetty,
    metadata: Option[ToteutusMetadata],
    sorakuvausId: Option[UUID],
    muokkaaja: UserOid,
    organisaatioOid: OrganisaatioOid,
    kielivalinta: Seq[Kieli],
    teemakuva: Option[String],
    modified: Option[Modified],
    koulutuksetKoodiUri: Seq[String] = Seq.empty
) extends PerustiedotWithOid[ToteutusOid, Toteutus] {
  override def withMuokkaaja(muokkaaja: UserOid): Toteutus = copy(muokkaaja = muokkaaja)
}
