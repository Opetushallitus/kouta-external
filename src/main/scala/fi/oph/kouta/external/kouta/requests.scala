package fi.oph.kouta.external.kouta

import fi.oph.kouta.external.domain.{Haku, Koulutus}
import fi.oph.kouta.servlet.Authenticated

case class KoutaHakuRequest(authenticated: Authenticated, haku: Haku)

case class KoutaKoulutusRequest(authenticated: Authenticated, koulutus: Koulutus)
