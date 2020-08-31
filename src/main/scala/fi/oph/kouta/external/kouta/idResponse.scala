package fi.oph.kouta.external.kouta

import java.util.UUID

import fi.oph.kouta.external.domain.oid.GenericOid

sealed trait IdResponse

case class UuidResponse(id: UUID) extends IdResponse

case class OidResponse(oid: GenericOid) extends IdResponse
