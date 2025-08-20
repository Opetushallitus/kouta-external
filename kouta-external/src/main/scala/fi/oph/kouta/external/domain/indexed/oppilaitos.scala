package fi.oph.kouta.external.domain.indexed

import fi.oph.kouta.domain.oid.OrganisaatioOid
import fi.oph.kouta.external.domain._

case class OppilaitosIndexed(
  oid: OrganisaatioOid,
  nimi: Option[Kielistetty],
  oppilaitos: Option[OppilaitosOppilaitosIndexed]
)

case class OppilaitosOppilaitosIndexed(
  metadata: Option[OppilaitosMetadataIndexed]
)

case class OppilaitosMetadataIndexed(
  esittely: Option[Kielistetty],
  yhteystiedot: Option[OppilaitosYhteystiedotIndexed]
)

case class OppilaitosYhteystiedotIndexed(
  postiosoiteStr: Option[Kielistetty],
  kayntiosoiteStr: Option[Kielistetty]
)
