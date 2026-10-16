package fi.oph.kouta.external.integration

import fi.oph.kouta.external.database.{KoutaDatabase, SQLHelpers}
import fi.oph.kouta.external.{KoutaConfigurationFactory, TestSetups}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import slick.jdbc.PostgresProfile.api._

import java.util.UUID

class SessionCleanupSpec extends AnyFlatSpec with Matchers with BeforeAndAfterEach with SQLHelpers {
  KoutaConfigurationFactory.setupWithDefaultTemplateFile()
  TestSetups.setupPostgres()

  private lazy val db = KoutaDatabase

  private val cleanupJobName = "session-cleanup"

  private val expiredSession = UUID.randomUUID()
  private val activeSession  = UUID.randomUUID()

  private def storeSession(id: UUID, lastReadAgo: String): Unit = {
    db.runBlocking(
      sqlu"""insert into sessions (id, cas_ticket, person, last_read)
             values ($id, ${s"ST-$id"}, 'test-person-oid', now() - $lastReadAgo::interval)"""
    )
    db.runBlocking(
      sqlu"""insert into authorities (session, authority)
             values ($id, 'APP_KOUTA_READ_1.2.246.562.10.00000000001'),
                    ($id, 'APP_KOUTA_CRUD_1.2.246.562.10.00000000001')"""
    )
  }

  private def sessionCount(id: UUID): Int =
    db.runBlocking(sql"""select count(*) from sessions where id = $id""".as[Int].head)

  private def authorityCount(id: UUID): Int =
    db.runBlocking(sql"""select count(*) from authorities where session = $id""".as[Int].head)

  /** Ajaa sen SQL:n, jonka V4-migraatio on ajastanut pg_croniin, jotta testi ei pääse
    * eriytymään varsinaisesta siivouksesta.
    */
  private def runScheduledCleanup(): Unit = {
    val command = db.runBlocking(
      sql"""select command from cron.job where jobname = $cleanupJobName""".as[String].head
    )
    db.runBlocking(sqlu"#$command")
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    storeSession(expiredSession, "3 days")
    storeSession(activeSession, "10 minutes")
  }

  override def afterEach(): Unit = {
    db.runBlocking(sqlu"""delete from sessions where id in ($expiredSession, $activeSession)""")
    super.afterEach()
  }

  "Sessioiden siivous" should "olla ajastettuna pg_cronilla" in {
    val schedules = db.runBlocking(
      sql"""select schedule from cron.job where jobname = $cleanupJobName""".as[String]
    )
    schedules should contain only "0 0 */2 * *"
  }

  it should "poistaa vanhentuneen session" in {
    runScheduledCleanup()
    sessionCount(expiredSession) shouldBe 0
  }

  it should "poistaa vanhentuneen session authorities-rivit" in {
    authorityCount(expiredSession) shouldBe 2
    runScheduledCleanup()
    authorityCount(expiredSession) shouldBe 0
  }

  it should "jättää voimassa olevan session ja sen authorities-rivit rauhaan" in {
    runScheduledCleanup()
    sessionCount(activeSession) shouldBe 1
    authorityCount(activeSession) shouldBe 2
  }
}
