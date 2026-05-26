package fi.oph.kouta.external.client

import fi.oph.kouta.external.{KoutaConfigurationFactory, S3Configuration}
import fi.oph.kouta.external.domain.KoutaLightKoulutus
import fi.oph.kouta.external.util.KoutaLightJsonFormats
import fi.vm.sade.valinta.dokumenttipalvelu.SiirtotiedostoPalvelu
import org.json4s.jackson.Serialization.writePretty

import java.io.ByteArrayInputStream
import java.util.UUID

object SiirtotiedostoPalveluClient extends SiirtotiedostoPalveluClient(KoutaConfigurationFactory.configuration.ovaraKoutaLightConfiguration.s3Configuration)

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
