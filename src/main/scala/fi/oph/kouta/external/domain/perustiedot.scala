package fi.oph.kouta.external.domain

import java.util.UUID

import fi.oph.kouta.domain.oid._
import fi.oph.kouta.domain.{Julkaisutila, Kieli, Modified}
import fi.oph.kouta.security.AuthorizableEntity

sealed trait Perustiedot[ID, T] extends AuthorizableEntity[T] {
  val tila: Julkaisutila
  val nimi: Kielistetty
  val muokkaaja: UserOid
  val kielivalinta: Seq[Kieli]
  val organisaatioOid: OrganisaatioOid
  val modified: Option[Modified]
}

abstract class PerustiedotWithOid[ID <: Oid, T] extends Perustiedot[ID, T] {
  val oid: Option[Oid]
}

abstract class PerustiedotWithId[T] extends Perustiedot[UUID, T] {
  val id: Option[UUID]
}
