package fi.oph.kouta.external.domain.indexed

import java.util.UUID

import fi.oph.kouta.domain.{Julkaisutila, Kieli, Koulutustyyppi, Modified}
import fi.oph.kouta.external.domain.{Kielistetty, Sorakuvaus, SorakuvausMetadata}

case class SorakuvausIndexed(
    id: Option[UUID],
    tila: Julkaisutila,
    nimi: Kielistetty,
    koulutustyyppi: Koulutustyyppi,
    kielivalinta: Seq[Kieli],
    metadata: Option[SorakuvausMetadataIndexed],
    organisaatio: Organisaatio,
    muokkaaja: Muokkaaja,
    modified: Option[Modified]
) {
  def toSorakuvaus: Sorakuvaus = Sorakuvaus(
    id = id,
    tila = tila,
    nimi = nimi,
    koulutustyyppi = koulutustyyppi,
    kielivalinta = kielivalinta,
    metadata = metadata.map(_.toSorakuvausMetadata),
    organisaatioOid = organisaatio.oid,
    muokkaaja = muokkaaja.oid,
    modified = modified
  )
}

class SorakuvausMetadataIndexed(kuvaus: Kielistetty,
                                koulutusala: Option[KoodiUri],
                                koulutus: Seq[KoodiUri] = Seq()
                              ) {
  def toSorakuvausMetadata: SorakuvausMetadata = SorakuvausMetadata(
    kuvaus = kuvaus,
    koulutusalaKoodiUri = koulutusala.map(_.koodiUri),
    koulutusKoodiUrit = koulutus.map(_.koodiUri)
  )
}
