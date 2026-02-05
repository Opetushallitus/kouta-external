package fi.oph.kouta.external.integration

import fi.oph.kouta.external.TestData.KoutaLightKoulutusWithOptionalData
import fi.oph.kouta.external.integration.fixture.KoutaLightFixture
import fi.oph.kouta.koutalight.domain.ExternalKoutaLightKoulutus

import java.time.temporal.ChronoUnit
import java.time.{Duration, LocalDateTime}

class KoutaLightSiirtotiedostoSpec extends KoutaLightFixture {
  val Path = "kouta-light-koulutukset"

  val dayBefore     = Some(LocalDateTime.now.minus(Duration.of(1, ChronoUnit.DAYS)))
  val twoDaysBefore = Some(LocalDateTime.now.minus(Duration.of(2, ChronoUnit.DAYS)))
  val dayAfter      = Some(LocalDateTime.now.plus(Duration.of(1, ChronoUnit.DAYS)))

  val koutaLightKoulutus1: ExternalKoutaLightKoulutus =
    KoutaLightKoulutusWithOptionalData.copy(externalId = "externalId1")
  val koutaLightKoulutus2: ExternalKoutaLightKoulutus =
    KoutaLightKoulutusWithOptionalData.copy(externalId = "externalId2")
  val koutaLightKoulutus3: ExternalKoutaLightKoulutus =
    KoutaLightKoulutusWithOptionalData.copy(externalId = "externalId3")

  s"Create kouta-light-koulutukset-siirtotiedostot" should "fail to create koulutukset transfer file without OPH Pääkäyttäjä role" in {
    get(Path, dayBefore, dayAfter, koutaLightSessionId1, 403)
  }

  it should "succeed to split 3 koulutus entities in two koulutukset transfer files" in {
    put(List(koutaLightKoulutus1), koutaLightSessionId1)
    put(List(koutaLightKoulutus2), koutaLightSessionId1)
    put(List(koutaLightKoulutus3), koutaLightSessionId1)
    get(
      Path,
      dayBefore,
      dayAfter,
      ophPaakayttajaSessionId,
      200
    ) shouldEqual s"""{"keys":"koutalight_koulutus__1.json, koutalight_koulutus__2.json","count":"3","success":"true"}"""
  }

  it should "Save koulutukset ok without timerange" in {
    get(
      Path,
      None,
      None,
      ophPaakayttajaSessionId,
      200
    ) shouldEqual s"""{"keys":"koutalight_koulutus__1.json, koutalight_koulutus__2.json","count":"3","success":"true"}"""
  }

  it should "return error when trying to create siirtotiedosto with invalid time definition " in {
    get(
      s"$KoutaLightSiirtotiedostoPath/$Path?startTime=puppua",
      headers = Seq(sessionHeader(ophPaakayttajaSessionId))
    ) {
      status should equal(400)
    }
  }

  it should "return error return error when trying to create siirtotiedosto with starttime in future" in {
    get(Path, dayAfter, None, ophPaakayttajaSessionId, 400)
  }

  it should "return error when trying to create siirtotiedosto with illegal timerange" in {
    get(Path, dayBefore, twoDaysBefore, ophPaakayttajaSessionId, 400)
  }
}
