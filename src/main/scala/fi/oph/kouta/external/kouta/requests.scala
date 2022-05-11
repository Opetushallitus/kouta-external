package fi.oph.kouta.external.kouta

import fi.oph.kouta.external.domain.{Haku, Hakukohde, Koulutus, Sorakuvaus, Toteutus, Valintaperuste}
import fi.oph.kouta.servlet.Authenticated

trait ExternalRequest {
  val authenticated: Authenticated
}

case class KoutaKoulutusRequest(authenticated: Authenticated, koulutus: Koulutus) extends ExternalRequest

case class KoutaToteutusRequest(authenticated: Authenticated, toteutus: Toteutus) extends ExternalRequest

case class KoutaHakuRequest(authenticated: Authenticated, haku: Haku) extends ExternalRequest

case class KoutaHakukohdeRequest(authenticated: Authenticated, hakukohde: Hakukohde) extends ExternalRequest

case class KoutaValintaperusteRequest(authenticated: Authenticated, valintaperuste: Valintaperuste) extends ExternalRequest

case class KoutaSorakuvausRequest(authenticated: Authenticated, sorakuvaus: Sorakuvaus) extends ExternalRequest

