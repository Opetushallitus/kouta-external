package fi.oph.kouta.koutalight.client

import fi.oph.kouta.koutalight.domain.KoutaLightKoulutusWithMetadata
import fi.oph.kouta.koutalight.util.KoutaLightJsonFormats
import fi.oph.kouta.koutalight.{OvaraKoutaLightConfiguration, S3Configuration}
import fi.vm.sade.valinta.dokumenttipalvelu.SiirtotiedostoPalvelu
import org.json4s.jackson.Serialization.writePretty

import java.io.ByteArrayInputStream
import java.util.UUID

object SiirtotiedostoPalveluClient extends SiirtotiedostoPalveluClient

class SiirtotiedostoPalveluClient extends KoutaLightJsonFormats {
  val config: S3Configuration = OvaraKoutaLightConfiguration.s3Configuration
  private val siirtotiedostoPalvelu = new SiirtotiedostoPalvelu(
    config.region.getOrElse("eu-west-1"),
    config.transferFileBucket,
    config.transferFileTargetRoleArn
  )
  private val saveRetryCount = config.transferFileSaveRetryCount

  def saveSiirtotiedosto(
      contentType: String,
      content: Seq[KoutaLightKoulutusWithMetadata],
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
