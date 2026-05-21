package fi.oph.kouta.external.service

import fi.oph.kouta.domain.Kieli
import fi.oph.kouta.domain.oid.OrganisaatioOid
import fi.oph.kouta.external.database.KoutaLightDAO
import fi.oph.kouta.external.domain.enums.KoutaLightMassResult
import fi.oph.kouta.external.domain.enums.Operation.Upsert
import fi.oph.kouta.koutalight.domain.ExternalKoutaLightKoulutus
import fi.oph.kouta.logging.Logging

import scala.util.{Failure, Success}

case class ValidationError(koulutusExternalId: String, message: String)

object Validations {
  def and(validations: Seq[ValidationError]*): Seq[ValidationError] = validations.flatten.distinct

  def findMissingLanguages(kielivalinta: Seq[Kieli], kielistetty: Map[Kieli, _]): Seq[Kieli] =
    kielivalinta.filter(kieli => !kielistetty.keys.toSeq.contains(kieli))

  def validateKielistetty(
      kielivalinta: Seq[Kieli],
      kielistetty: Map[Kieli, _],
      koulutusExternalId: String,
      propertyName: String
  ): Seq[ValidationError] = {
    Validations.findMissingLanguages(kielivalinta, kielistetty) match {
      case missingLanguages if missingLanguages.nonEmpty =>
        List(ValidationError(koulutusExternalId, Validations.invalidKielistetty(missingLanguages, propertyName)))
      case _ => List()
    }
  }

  def validateOptionalKielistetty(
      kielivalinta: Seq[Kieli],
      kielistetty: Map[Kieli, _],
      koulutusExternalId: String,
      propertyName: String
  ): Seq[ValidationError] = {
    if (kielistetty.nonEmpty)
      Validations.validateKielistetty(kielivalinta, kielistetty, koulutusExternalId, propertyName)
    else List()
  }

  def validateOpetuskielet(koulutusExternalId: String, opetuskielet: Seq[String]): Seq[ValidationError] = {
    val invalidKielikoodit = opetuskielet.filter(kieli => kieli.length < 2 || kieli.length > 3)

    if (invalidKielikoodit.nonEmpty)
      List(ValidationError(koulutusExternalId, invalidOpetuskielet(invalidKielikoodit)))
    else
      List()
  }

  private def invalidKielistetty(values: Seq[Kieli], propertyName: String) =
    s"Kielistetystä kentästä '$propertyName' puuttuu arvo kielillä [${values.mkString(", ")}]"

  private def invalidOpetuskielet(values: Seq[String]) =
    s"Virheellinen arvo [${values.mkString(", ")}] kentässä 'opetuskielet'"
}

object KoutaLightService extends KoutaLightService

class KoutaLightService extends Logging {
  def validate(koulutus: ExternalKoutaLightKoulutus): Seq[ValidationError] = {
    val kielivalinta       = koulutus.kielivalinta
    val koulutusExternalId = koulutus.externalId
    Validations.and(
      Validations.validateKielistetty(kielivalinta, koulutus.nimi, koulutusExternalId, "nimi"),
      Validations.validateKielistetty(kielivalinta, koulutus.kuvaus, koulutusExternalId, "kuvaus"),
      koulutus.tarjoajat.zipWithIndex.flatMap(tarjoaja =>
        Validations
          .validateKielistetty(kielivalinta, tarjoaja._1, koulutusExternalId, s"tarjoajat[${tarjoaja._2}]")
      ),
      koulutus.ammattinimikkeet.zipWithIndex.flatMap(ammattinimike =>
        Validations.validateKielistetty(
          kielivalinta,
          ammattinimike._1,
          koulutusExternalId,
          s"ammattinimikkeet[${ammattinimike._2}]"
        )
      ),
      koulutus.asiasanat.zipWithIndex.flatMap(asiasana =>
        Validations
          .validateKielistetty(kielivalinta, asiasana._1, koulutusExternalId, s"asiasanat[${asiasana._2}]")
      ),
      Validations.validateOptionalKielistetty(
        kielivalinta,
        koulutus.maksullisuuskuvaus,
        koulutusExternalId,
        "maksullisuuskuvaus"
      ),
      Validations.validateOptionalKielistetty(
        kielivalinta,
        koulutus.hakulomakeLinkki,
        koulutusExternalId,
        "hakulomakeLinkki"
      ),
      Validations.validateOpetuskielet(koulutusExternalId, koulutus.opetuskielet)
    )
  }

  private def validationErrorsToKoutaLightMassResultError(
      validationErrors: Seq[ValidationError]
  ): Seq[KoutaLightMassResult] =
    validationErrors.map(error =>
      KoutaLightMassResult.Error(Upsert, Option(error.koulutusExternalId), exception = error.message)
    )

  def put(
      koulutukset: List[ExternalKoutaLightKoulutus],
      organisaatioOid: OrganisaatioOid
  ): Seq[KoutaLightMassResult] = {
    koulutukset.flatMap(koulutus => {
      val externalId       = koulutus.externalId
      val validationErrors = validate(koulutus)
      if (validationErrors.isEmpty) {
        KoutaLightDAO.createOrUpdate(koulutus, organisaatioOid) match {
          case Success(null) =>
            logger.info(s"Created koulutus with externalId: ${koulutus.externalId}, ownerOrg: $organisaatioOid")
            List(KoutaLightMassResult.CreateSuccess(externalId = Some(externalId)))
          case Success(_) =>
            logger.info(s"Updated koulutus with externalId: ${koulutus.externalId}, ownerOrg: $organisaatioOid")
            List(KoutaLightMassResult.UpdateSuccess(externalId = Some(externalId)))
          case Failure(e) =>
            logger.error(
              s"Create or update failed on koulutus with externalId: ${koulutus.externalId}, ownerOrg: $organisaatioOid",
              e
            )
            List(
              KoutaLightMassResult.Error(
                operation = Upsert,
                externalId = Some(externalId),
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
