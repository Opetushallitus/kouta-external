package fi.oph.kouta.external.auditlog

import fi.vm.sade.auditlog.Operation

abstract class AuditOperation(val name: String) extends Operation

object AuditOperation {
  case object Login                          extends AuditOperation("kirjautuminen")
  case object KoutaLightSiirtotiedostoCreate extends AuditOperation("kouta_light_siirtotiedosto_create")
}

trait AuditResourceOperations {
  def name: String
  case object Create extends AuditOperation(s"${name}_create")
  case object Update extends AuditOperation(s"${name}_update")
}
