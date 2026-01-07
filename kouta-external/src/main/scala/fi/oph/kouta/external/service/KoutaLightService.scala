package fi.oph.kouta.external.service

import fi.oph.kouta.domain.Kieli
import fi.oph.kouta.domain.oid.OrganisaatioOid
import fi.oph.kouta.external.database.KoutaLightDAO
import fi.oph.kouta.external.domain.Kielistetty
import fi.oph.kouta.external.domain.enums.{KoutaLightMassResult, Operation}
import fi.oph.kouta.external.domain.koutalight.KoutaLightKoulutus
import fi.oph.kouta.logging.Logging

import scala.util.{Failure, Success}

case class ValidationError(koulutusExternalId: String, message: String)

object Validations {
  def findMissingLanguages(kielivalinta: Seq[Kieli], kielistetty: Kielistetty): Seq[Kieli] =
    for {
      kieli <- kielivalinta
      if !kielistetty.keys.toSeq.contains(kieli)
    } yield kieli

  def validateKielistetty(
      kielivalinta: List[Kieli],
      kielistetty: Kielistetty,
      koulutusExternalId: String,
      propertyName: String
  ): Seq[ValidationError] = {
    Validations.findMissingLanguages(kielivalinta, kielistetty) match {
      case missingLanguages if missingLanguages.nonEmpty =>
        List(ValidationError(koulutusExternalId, Validations.invalidKielistetty(missingLanguages, propertyName)))
      case _ => List()
    }
  }

  def invalidKielistetty(values: Seq[Kieli], propertyName: String) =
    s"Kielistetystä kentästä ${'"'}$propertyName${'"'} puuttuu arvo kielillä [${values.mkString(",")}]"
}

object KoutaLightService extends KoutaLightService

class KoutaLightService extends Logging {
  def put(koulutukset: List[KoutaLightKoulutus], organisaatioOid: OrganisaatioOid): Seq[KoutaLightMassResult] = {
    koulutukset.map(koulutus => {
      val externalId = koulutus.externalId
      KoutaLightDAO.createOrUpdate(koulutus, organisaatioOid) match {
        case Success(null) =>
          logger.info(s"Created koulutus ${koulutus.externalId}")
          KoutaLightMassResult.CreateSuccess(externalId = Some(externalId))
        case Success(_) =>
          logger.info(s"Updated koulutus ${koulutus.externalId}")
          KoutaLightMassResult.UpdateSuccess(externalId = Some(externalId))
        case Failure(e) =>
          logger.error(s"Create or update failed on koulutus with externalId ${koulutus.externalId}", e)
          KoutaLightMassResult.Error(operation = Operation.Upsert, externalId = Some(externalId), exception = e)
      }
    })
  }
}
