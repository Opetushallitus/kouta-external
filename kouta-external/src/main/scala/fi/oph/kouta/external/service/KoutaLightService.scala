package fi.oph.kouta.external.service

import fi.oph.kouta.domain.oid.OrganisaatioOid
import fi.oph.kouta.external.database.KoutaLightDAO
import fi.oph.kouta.external.domain.enums.{KoutaLightMassResult, Operation}
import fi.oph.kouta.external.domain.koutalight.KoutaLightKoulutus
import fi.oph.kouta.logging.Logging

import scala.util.{Failure, Success}

object KoutaLightService extends KoutaLightService

class KoutaLightService extends Logging {
  def put(koulutukset: List[KoutaLightKoulutus], organisaatioOid: OrganisaatioOid): Seq[KoutaLightMassResult] = {
    koulutukset.map(koulutus => {
      val externalId = koulutus.externalId
      KoutaLightDAO.createOrUpdate(koulutus, organisaatioOid) match {
        case Success(null) =>
          KoutaLightMassResult.CreateSuccess(externalId = Some(externalId))
        case Success(_) =>
          KoutaLightMassResult.UpdateSuccess(externalId = Some(externalId))
        case Failure(e) =>
          KoutaLightMassResult.Error(operation = Operation.Upsert, externalId = Some(externalId), exception = e)
      }
    })
  }
}
