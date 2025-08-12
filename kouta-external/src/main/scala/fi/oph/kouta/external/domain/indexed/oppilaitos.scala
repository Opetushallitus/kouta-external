package fi.oph.kouta.external.domain.indexed

import fi.oph.kouta.domain.oid.OrganisaatioOid
import fi.oph.kouta.external.domain._

case class OppilaitosIndexed(
  oid: OrganisaatioOid,
  oppilaitos: Option[OppilaitosOppilaitosIndexed]
)

case class OppilaitosOppilaitosIndexed(
  metadata: Option[OppilaitosMetadataIndexed]
)

case class OppilaitosMetadataIndexed(
  esittely: Kielistetty,
  yhteystiedot: OppilaitosYhteystiedotIndexed
)

case class OppilaitosYhteystiedotIndexed(
  postiosoiteStr: Kielistetty,
  kayntiosoiteStr: Kielistetty
)
