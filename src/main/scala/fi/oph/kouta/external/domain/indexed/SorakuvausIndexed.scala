package fi.oph.kouta.external.domain.indexed

import java.util.UUID

import fi.oph.kouta.domain.{Julkaisutila, Kieli, Koulutustyyppi, Modified}
import fi.oph.kouta.external.domain.{Kielistetty, Sorakuvaus, SorakuvausMetadata}

case class SorakuvausIndexed(
    id: Option[UUID],
    tila: Julkaisutila,
    nimi: Kielistetty,
    koulutustyyppi: Koulutustyyppi,
    julkinen: Boolean,
    kielivalinta: Seq[Kieli],
    metadata: Option[SorakuvausMetadata],
    organisaatio: Organisaatio,
    muokkaaja: Muokkaaja,
    modified: Option[Modified]
) {
  def toSorakuvaus: Sorakuvaus = Sorakuvaus(
    id = id,
    tila = tila,
    nimi = nimi,
    koulutustyyppi = koulutustyyppi,
    julkinen = julkinen,
    kielivalinta = kielivalinta,
    metadata = metadata,
    organisaatioOid = organisaatio.oid,
    muokkaaja = muokkaaja.oid,
    modified = modified
  )
}
