package fi.oph.kouta.external.security

import fi.oph.kouta.security.Authority

case class KayttooikeusUserDetails(authorities: Set[Authority], oid: String)
