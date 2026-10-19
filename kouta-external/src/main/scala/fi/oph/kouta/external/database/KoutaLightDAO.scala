package fi.oph.kouta.external.database

import fi.oph.kouta.domain.oid.OrganisaatioOid
import fi.oph.kouta.external.database.KoutaDatabase.runBlockingTransactionally
import fi.oph.kouta.external.domain.{ExternalKoutaLightKoulutus, KoutaLightKoulutus}
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

/**
 * @param before koulutuksen tallennettu tila JSON-muodossa ennen tallennusta, None jos koulutus luotiin
 * @param after koulutuksen tallennettu tila JSON-muodossa tallennuksen jälkeen
 */
case class KoutaLightUpsertResult(before: Option[String], after: String) {
  def created: Boolean = before.isEmpty
}

object KoutaLightDAO extends KoutaLightSQL {
  /**
   *
   * @param koulutus ExternalKoutaLightKoulutus to save to db
   * @param organisaatioOid Oid of the organization that is mapped with the API user i.e. owner org
   * @param auditLog audit-logitus, joka ajetaan samassa transaktiossa tallennuksen kanssa
   * @return koulutuksen tila ennen ja jälkeen tallennuksen
   */
  def createOrUpdate(koulutus: ExternalKoutaLightKoulutus, organisaatioOid: OrganisaatioOid)(
      auditLog: KoutaLightUpsertResult => Unit
  ): Try[KoutaLightUpsertResult] = {
    val koulutusToCreate = KoutaLightKoulutus(organisaatioOid, koulutus)
    runBlockingTransactionally(
      for {
        before <- selectAuditJson(koulutusToCreate)
        after  <- upsert(koulutusToCreate)
        result = KoutaLightUpsertResult(before, after)
        _ <- Try(auditLog(result)).fold(DBIO.failed, DBIO.successful)
      } yield result
    )
  }
}

sealed trait KoutaLightSQL extends SQLHelpers {
  private val auditJson =
    "jsonb_build_object('kielivalinta', kielivalinta, 'tila', tila, 'nimi', nimi, 'tarjoajat', tarjoajat, 'metadata', metadata)::text"

  def selectAuditJson(koulutus: KoutaLightKoulutus): DBIO[Option[String]] =
    sql"""select #$auditJson
          from kouta_light_koulutus
          where owner_org = ${koulutus.ownerOrg} and external_id = ${koulutus.externalId}""".as[String].headOption

  def upsert(koulutus: KoutaLightKoulutus): DBIO[String] = {
    sql"""insert into kouta_light_koulutus (
      external_id,
      kielivalinta,
      tila,
      nimi,
      tarjoajat,
      metadata,
      owner_org
    ) values (
      ${koulutus.externalId},
      ${toJsonParam(koulutus.kielivalinta)}::jsonb,
      ${koulutus.tila},
      ${toJsonParam(koulutus.nimi)}::jsonb,
      ${toJsonParam(koulutus.tarjoajat)}::jsonb,
      ${toJsonParam(koulutus.metadata)}::jsonb,
      ${koulutus.ownerOrg}
    )
    on conflict (owner_org, external_id)
    do update set
      kielivalinta = EXCLUDED.kielivalinta,
      tila = EXCLUDED.tila,
      nimi = EXCLUDED.nimi,
      tarjoajat = EXCLUDED.tarjoajat,
      metadata = EXCLUDED.metadata,
      updated_at = now()
    returning #$auditJson""".as[String].head
  }
}
