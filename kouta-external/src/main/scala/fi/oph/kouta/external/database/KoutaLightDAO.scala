package fi.oph.kouta.external.database

import fi.oph.kouta.domain.oid.OrganisaatioOid
import fi.oph.kouta.external.database.KoutaDatabase.runBlockingTransactionally
import fi.oph.kouta.external.domain.koutalight.{KoutaLightKoulutus, KoutaLightKoulutusWithMetadata}
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._

import scala.util.Try

object KoutaLightDAO extends KoutaLightSQL {
  def createOrUpdate(koulutus: KoutaLightKoulutus, organisaatioOid: OrganisaatioOid): Try[String] = {
    val koulutusToCreate = KoutaLightKoulutusWithMetadata(organisaatioOid, koulutus)
    runBlockingTransactionally(upsert(koulutusToCreate))
  }
}

sealed trait KoutaLightSQL extends SQLHelpers {
  def upsert(koulutus: KoutaLightKoulutusWithMetadata): DBIO[String] = {
    sql"""insert into koulutus (
      external_id,
      kielivalinta,
      tila,
      nimi,
      metadata,
      owner_org
    ) values (
      ${koulutus.externalId},
      ${toJsonParam(koulutus.kielivalinta)}::jsonb,
      ${koulutus.tila},
      ${toJsonParam(koulutus.nimi)}::jsonb,
      ${toJsonParam(koulutus.metadata)}::jsonb,
      ${koulutus.ownerOrg}
    )
    on conflict (owner_org, external_id)
    do update set
      kielivalinta = EXCLUDED.kielivalinta,
      tila = EXCLUDED.tila,
      nimi = EXCLUDED.nimi,
      metadata = EXCLUDED.metadata,
      updated_at = now()
    returning updated_at""".as[String].head
  }
}
