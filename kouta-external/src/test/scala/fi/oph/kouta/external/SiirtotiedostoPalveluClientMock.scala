package fi.oph.kouta.external

import fi.oph.kouta.koutalight.S3Configuration
import fi.oph.kouta.koutalight.client.SiirtotiedostoPalveluClient
import fi.oph.kouta.koutalight.domain.KoutaLightKoulutus

import java.util.UUID

class SiirtotiedostoPalveluClientMock
    extends SiirtotiedostoPalveluClient(
      S3Configuration(
        transferFileBucket = "",
        transferFileTargetRoleArn = "",
        region = None,
        1,
        2
      )
    ) {
  override def saveSiirtotiedosto(
      contentType: String,
      content: Seq[KoutaLightKoulutus],
      operationId: UUID,
      operationSubId: Int
  ): String = {
    s"koutalight_koulutus__$operationSubId.json"
  }
}
