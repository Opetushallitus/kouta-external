package fi.oph.kouta.external.auditlog

import fi.oph.kouta.external.domain

abstract class AuditResource(val name: String, val idField: String) extends AuditResourceOperations {
  override def toString: String = name
}

object AuditResource {
  case object Koulutus           extends AuditResource("koulutus", "oid")
  case object Toteutus           extends AuditResource("toteutus", "oid")
  case object Haku               extends AuditResource("haku", "oid")
  case object Hakukohde          extends AuditResource("hakukohde", "oid")
  case object Valintaperuste     extends AuditResource("valintaperuste", "id")
  case object Sorakuvaus         extends AuditResource("sorakuvaus", "id")
  case object KoutaLightKoulutus extends AuditResource("kouta_light_koulutus", "externalId")

  def apply(entity: AnyRef): AuditResource =
    entity match {
      case _: domain.Koulutus       => Koulutus
      case _: domain.Toteutus       => Toteutus
      case _: domain.Haku           => Haku
      case _: domain.Hakukohde      => Hakukohde
      case _: domain.Valintaperuste => Valintaperuste
      case _: domain.Sorakuvaus     => Sorakuvaus
      case _                        => throw new IllegalArgumentException(s"Tuntematon audit-resurssi: ${entity.getClass.getSimpleName}")
    }
}
