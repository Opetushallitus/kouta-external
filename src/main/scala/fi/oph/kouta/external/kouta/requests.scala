package fi.oph.kouta.external.kouta

import fi.oph.kouta.external.domain.{Haku, Hakukohde, Koulutus, Sorakuvaus, Toteutus, Valintaperuste}
import fi.oph.kouta.servlet.Authenticated

trait ExternalRequest[T] {
  val authenticated: Authenticated
  val entity: T
}

case class KoutaKoulutusRequest(authenticated: Authenticated, entity: Koulutus) extends ExternalRequest[Koulutus]

case class KoutaToteutusRequest(authenticated: Authenticated, entity: Toteutus) extends ExternalRequest[Toteutus]

case class KoutaHakuRequest(authenticated: Authenticated, entity: Haku) extends ExternalRequest[Haku]

case class KoutaHakukohdeRequest(authenticated: Authenticated, entity: Hakukohde) extends ExternalRequest[Hakukohde]

case class KoutaValintaperusteRequest(authenticated: Authenticated, entity: Valintaperuste) extends ExternalRequest[Valintaperuste]

case class KoutaSorakuvausRequest(authenticated: Authenticated, entity: Sorakuvaus) extends ExternalRequest[Sorakuvaus]

