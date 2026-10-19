package fi.oph.kouta.external

import fi.oph.kouta.external.auditlog.AuditLog
import fi.vm.sade.auditlog.Logger

object MockAuditLogger extends Logger {
  val logs = scala.collection.mutable.ArrayBuffer[String]()

  def log(log: String): Unit = logs.synchronized {
    logs += log
  }

  def clean(): Unit = logs.synchronized {
    logs.clear()
  }

  def find(first: String, searchStrings: String*): Option[String] = logs.synchronized {
    logs.find(log => (first +: searchStrings).forall(log.contains))
  }
}

object MockAuditLog extends AuditLog(MockAuditLogger)
