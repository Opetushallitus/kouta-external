package fi.oph.kouta.external.domain

import java.util.UUID
import fi.oph.kouta.domain.oid._
import fi.oph.kouta.domain.{En, Fi, Julkaisutila, Kieli, Modified, Sv}
import fi.oph.kouta.external.domain.indexed.{KoodiUri, OsoiteES, OsoiteIndexed, PostinumeroKoodiES}
import fi.oph.kouta.security.AuthorizableEntity

trait WithTila {
  val tila: Julkaisutila
}
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

trait WithKielistettyOsoite {
  def toKielistettyOsoite(osoiteES: Option[OsoiteES]): Option[OsoiteIndexed] = {
    def toKielistettyMap(map: Map[String, String]): Kielistetty = {
      Map(
        En -> map.get("en"),
        Fi -> map.get("fi"),
        Sv -> map.get("sv")
      ).collect { case (k, Some(v)) => (k, v) }
    }

    def toKielistettyPostinumeroMap(map: Map[String, PostinumeroKoodiES]): KielistettyPostinumero = {
      Map(
        En -> map.get("en"),
        Fi -> map.get("fi"),
        Sv -> map.get("sv")
      ).collect { case (k, Some(v)) => (k, Postinumerokoodi(koodiUri = v.koodiUri, nimi = v.nimi)) }
    }

    osoiteES match {
      case Some(osoite) => Some(OsoiteIndexed(
        osoite = toKielistettyMap(osoite.osoite),
        postinumero = toKielistettyPostinumeroMap(osoite.postinumeroKoodiUri)
      ))
      case None => Some(OsoiteIndexed(osoite = Map(), postinumero = Map()))
    }
  }
}
