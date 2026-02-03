package fi.oph.kouta.koutalight.client

import fi.oph.kouta.koutalight.S3Configuration
import fi.oph.kouta.koutalight.domain.KoutaLightKoulutus
import fi.oph.kouta.koutalight.util.KoutaLightJsonFormats
import fi.vm.sade.valinta.dokumenttipalvelu.SiirtotiedostoPalvelu
import org.json4s.jackson.Serialization.writePretty

import java.io.ByteArrayInputStream
import java.util.UUID

class SiirtotiedostoPalveluClient(s3Configuration: S3Configuration) extends KoutaLightJsonFormats {
  val config: S3Configuration = s3Configuration

  private val siirtotiedostoPalvelu = new SiirtotiedostoPalvelu(
    config.region.getOrElse("eu-west-1"),
    config.transferFileBucket,
    config.transferFileTargetRoleArn
  )
  private val saveRetryCount = config.transferFileSaveRetryCount

  def saveSiirtotiedosto(
      contentType: String,
      content: Seq[KoutaLightKoulutus],
      operationId: UUID,
      operationSubId: Int
  ): String = {
    siirtotiedostoPalvelu
      .saveSiirtotiedosto(
        "koutalight",
        contentType,
        "",
        operationId.toString,
        operationSubId,
        new ByteArrayInputStream(writePretty(content).getBytes()),
        saveRetryCount
      )
      .key
  }
}
