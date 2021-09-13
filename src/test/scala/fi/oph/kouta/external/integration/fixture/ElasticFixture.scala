package fi.oph.kouta.external.integration.fixture

import com.sksamuel.elastic4s.ElasticApi.{RichFuture, catIndices, closeIndex, createRepository, createSnapshot, deleteSnapshot, recoverIndex, restoreSnapshot}
import com.sksamuel.elastic4s.ElasticDsl.{CatIndexesHandler, CloseIndexHandler, CreateRepositoryHandler, CreateSnapshotHandler, DeleteSnapshotHandler, IndexRecoveryHandler, RestoreSnapshotHandler}
import fi.oph.kouta.external.TempElasticClient.client
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats}

import scala.concurrent.ExecutionContext.Implicits.global

trait ElasticFixture extends Logging {
  lazy val testSnapshotRepository = {
    val repositoryName = "_testsnapshot"
    client.execute(createRepository(repositoryName, "fs").settings(Map("location" -> "snapshots"))).await
    repositoryName
  }

  def restoreState(snapshot: String): Unit = {
    implicit val formats: Formats = DefaultFormats

    def status = client.execute(catIndices("haku-kouta")).await.result(0).status

    def recoveryStatuses = {
      val recoveryBody = client.execute(recoverIndex("haku-kouta")).await.body.get
      val recoveryResponse = read[RecoveryResponse](recoveryBody)
      recoveryResponse.`haku-kouta`.shards.map(_.stage)
    }

    client.execute(closeIndex("haku-kouta")).await
    while (status != "close") Thread.sleep(50)

    client.execute(restoreSnapshot(snapshot, testSnapshotRepository).waitForCompletion(true)).await

    val start = System.currentTimeMillis

    //TODO: This still fails randomly on the next index access
    while (recoveryStatuses.isEmpty || !recoveryStatuses.forall(_ == "DONE")) {
      Thread.sleep(50)
    }

    logger.info(s"Recovery took ${System.currentTimeMillis - start} ms")
  }

  def saveState(snapshot: String): Unit = {
    client.execute(deleteSnapshot(snapshot, testSnapshotRepository)).await
    client.execute(createSnapshot(snapshot, testSnapshotRepository).waitForCompletion(true)).await
  }
}

case class Shard(id: Int, `type`: String, stage: String)

case class Shards(shards: Seq[Shard])

case class RecoveryResponse(`haku-kouta`: Shards)
