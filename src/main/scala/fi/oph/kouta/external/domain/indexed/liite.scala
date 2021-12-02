package fi.oph.kouta.external.domain.indexed

import java.time.LocalDateTime
import java.util.UUID

import fi.oph.kouta.domain.LiitteenToimitustapa
import fi.oph.kouta.external.domain.{Kielistetty, Liite, LiitteenToimitusosoite}

case class LiiteIndexed(
    id: Option[UUID],
    tyyppi: Option[KoodiUri],
    nimi: Kielistetty,
    kuvaus: Kielistetty,
    toimitusaika: Option[LocalDateTime],
    toimitustapa: Option[LiitteenToimitustapa],
    toimitusosoite: Option[LiitteenToimitusosoiteIndexed]
) {
  def toLiite: Liite = Liite(
    id = id,
    tyyppiKoodiUri = tyyppi.map(_.koodiUri),
    nimi = nimi,
    kuvaus = kuvaus,
    toimitusaika = toimitusaika,
    toimitustapa = toimitustapa,
    toimitusosoite = toimitusosoite.map(_.toLiitteenToimitusosoite)
  )
}

case class LiitteenToimitusosoiteIndexed(osoite: OsoiteIndexed, sahkoposti: Option[String], verkkosivu: Option[String]) {
  def toLiitteenToimitusosoite: LiitteenToimitusosoite =
    LiitteenToimitusosoite(osoite = osoite.toOsoite, sahkoposti = sahkoposti, verkkosivu = verkkosivu)
}
