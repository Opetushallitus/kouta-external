package fi.oph.kouta.external.service

import fi.oph.kouta.domain.oid.OrganisaatioOid
import fi.oph.kouta.external.auditlog.AuditLog
import fi.oph.kouta.external.database.{KoutaLightDAO, KoutaLightUpsertResult}
import fi.oph.kouta.external.domain.ExternalKoutaLightKoulutus
import fi.oph.kouta.external.domain.enums.KoutaLightMassResult
import fi.oph.kouta.external.domain.enums.Operation.{Create, Update, Upsert}
import fi.oph.kouta.logging.Logging
import fi.oph.kouta.servlet.Authenticated

import scala.util.{Failure, Success}

case class ValidationError(koulutusExternalId: String, message: String)

object Validations {
  def toValidationErrors(koulutusExternalId: String, validations: Seq[String]*): Seq[ValidationError] =
    validations.flatten.map(s => ValidationError(koulutusExternalId, s)).distinct

  def validateOpetuskielet(opetuskielet: Seq[String]): Seq[String] = {
    val invalidKielikoodit = opetuskielet.filter(kieli => kieli.length < 2 || kieli.length > 3)

    if (invalidKielikoodit.nonEmpty)
      List(invalidOpetuskielet(invalidKielikoodit))
    else
      List()
  }

  private def invalidOpetuskielet(values: Seq[String]) =
    s"Virheellinen arvo [${values.mkString(", ")}] kentässä 'opetuskielet'"
}

object KoutaLightService extends KoutaLightService(AuditLog)

class KoutaLightService(auditLog: AuditLog) extends Logging {
  def validate(koulutus: ExternalKoutaLightKoulutus): Seq[ValidationError] = {
    val koulutusExternalId = koulutus.externalId
    Validations.toValidationErrors(
      koulutusExternalId,
      Validations.validateOpetuskielet(koulutus.opetuskielet)
    )
  }

  private def validationErrorsToKoutaLightMassResultError(
      validationErrors: Seq[ValidationError]
  ): Seq[KoutaLightMassResult] =
    validationErrors.map(error =>
      KoutaLightMassResult.Error(Upsert, error.koulutusExternalId, exception = error.message)
    )

  private def logChange(externalId: String, organisaatioOid: OrganisaatioOid)(result: KoutaLightUpsertResult)(implicit
      authenticated: Authenticated
  ): Unit =
    result.before match {
      case None         => auditLog.logKoutaLightCreate(externalId, organisaatioOid, result.after)
      case Some(before) => auditLog.logKoutaLightUpdate(externalId, organisaatioOid, before, result.after)
    }

  def put(
      koulutukset: List[ExternalKoutaLightKoulutus],
      organisaatioOid: OrganisaatioOid
  )(implicit authenticated: Authenticated): Seq[KoutaLightMassResult] = {
    koulutukset.flatMap(koulutus => {
      val externalId       = koulutus.externalId
      val validationErrors = validate(koulutus)
      if (validationErrors.isEmpty) {
        KoutaLightDAO.createOrUpdate(koulutus, organisaatioOid)(logChange(externalId, organisaatioOid)) match {
          case Success(result) if result.created =>
            logger.info(s"Created koulutus with externalId: ${koulutus.externalId}, ownerOrg: $organisaatioOid")
            List(KoutaLightMassResult.Success(Create, externalId))
          case Success(_) =>
            logger.info(s"Updated koulutus with externalId: ${koulutus.externalId}, ownerOrg: $organisaatioOid")
            List(KoutaLightMassResult.Success(Update, externalId))
          case Failure(e) =>
            logger.error(
              s"Create or update failed on koulutus with externalId: ${koulutus.externalId}, ownerOrg: $organisaatioOid",
              e
            )
            List(
              KoutaLightMassResult.Error(
                operation = Upsert,
                externalId = externalId,
                exception = e.toString
              )
            )
        }
      } else {
        validationErrorsToKoutaLightMassResultError(validationErrors)
      }
    })
  }
}
