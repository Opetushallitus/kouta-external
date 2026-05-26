package fi.oph.kouta.external.service

import fi.oph.kouta.domain.Kieli
import fi.oph.kouta.domain.oid.OrganisaatioOid
import fi.oph.kouta.external.database.KoutaLightDAO
import fi.oph.kouta.external.domain.ExternalKoutaLightKoulutus
import fi.oph.kouta.external.domain.enums.KoutaLightMassResult
import fi.oph.kouta.external.domain.enums.Operation.{Create, Update, Upsert}
import fi.oph.kouta.logging.Logging

import scala.util.{Failure, Success}

case class ValidationError(koulutusExternalId: String, message: String)

object Validations {
  def toValidationErrors(koulutusExternalId: String, validations: Seq[String]*): Seq[ValidationError] =
    validations.flatten.map(s => ValidationError(koulutusExternalId, s)).distinct

  def findMissingLanguages(kielivalinta: Seq[Kieli], kielistetty: Map[Kieli, _]): Seq[Kieli] =
    kielivalinta.filter(kieli => !kielistetty.keys.toSeq.contains(kieli))

  def validateKielistetty(
      kielivalinta: Seq[Kieli],
      kielistetty: Map[Kieli, _],
      propertyName: String
  ): Seq[String] = {
    Validations.findMissingLanguages(kielivalinta, kielistetty) match {
      case missingLanguages if missingLanguages.nonEmpty =>
        List(Validations.invalidKielistetty(missingLanguages, propertyName))
      case _ => List()
    }
  }

  def validateOptionalKielistetty(
      kielivalinta: Seq[Kieli],
      kielistetty: Map[Kieli, _],
      propertyName: String
  ): Seq[String] = {
    if (kielistetty.nonEmpty)
      Validations.validateKielistetty(kielivalinta, kielistetty, propertyName)
    else List()
  }

  def validateOpetuskielet(opetuskielet: Seq[String]): Seq[String] = {
    val invalidKielikoodit = opetuskielet.filter(kieli => kieli.length < 2 || kieli.length > 3)

    if (invalidKielikoodit.nonEmpty)
      List(invalidOpetuskielet(invalidKielikoodit))
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
    Validations.toValidationErrors(
      koulutusExternalId,
      Validations.validateKielistetty(kielivalinta, koulutus.nimi, "nimi"),
      Validations.validateKielistetty(kielivalinta, koulutus.kuvaus, "kuvaus"),
      koulutus.tarjoajat.zipWithIndex.flatMap(tarjoajaWithIndex => {
        val (tarjoaja, index) = tarjoajaWithIndex
        Validations
          .validateKielistetty(kielivalinta, tarjoaja, s"tarjoajat[$index]")
      }),
      koulutus.ammattinimikkeet.zipWithIndex.flatMap(ammattinimikeWithIndex => {
        val (ammattinimike, index) = ammattinimikeWithIndex
        Validations.validateKielistetty(
          kielivalinta,
          ammattinimike,
          s"ammattinimikkeet[$index]"
        )
      }),
      koulutus.asiasanat.zipWithIndex.flatMap(asiasanaWithIndex => {
        val (asiasana, index) = asiasanaWithIndex
        Validations
          .validateKielistetty(kielivalinta, asiasana, s"asiasanat[$index]")
      }),
      Validations.validateOptionalKielistetty(
        kielivalinta,
        koulutus.maksullisuuskuvaus,
        "maksullisuuskuvaus"
      ),
      Validations.validateOptionalKielistetty(
        kielivalinta,
        koulutus.hakulomakeLinkki,
        "hakulomakeLinkki"
      ),
      Validations.validateOpetuskielet(koulutus.opetuskielet)
    )
  }

  private def validationErrorsToKoutaLightMassResultError(
      validationErrors: Seq[ValidationError]
  ): Seq[KoutaLightMassResult] =
    validationErrors.map(error =>
      KoutaLightMassResult.Error(Upsert, error.koulutusExternalId, exception = error.message)
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
