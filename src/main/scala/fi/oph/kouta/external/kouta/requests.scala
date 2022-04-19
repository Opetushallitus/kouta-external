package fi.oph.kouta.external.kouta

import fi.oph.kouta.external.domain.{Haku, Koulutus}
import fi.oph.kouta.servlet.Authenticated

trait ExternalRequest[T] {
  val authenticated: Authenticated
  val entity: T
  def vals(): (Option[Authenticated], T) = (Some(this.authenticated), this.entity)
}

case class KoutaHakuRequest(authenticated: Authenticated, entity: Haku) extends ExternalRequest[Haku]

case class KoutaKoulutusRequest(authenticated: Authenticated, entity: Koulutus) extends ExternalRequest[Koulutus]
