package fi.oph.kouta.external.client

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import java.nio.file.{Files, Paths}
import scala.collection.JavaConverters._

class AsyncHttpClientFactorySpec extends AnyFlatSpec with Matchers {

  /**
   * Tehdas ja tämä testi saavat luoda AHC-clientin suoraan, muut eivät.
   */
  private val AllowedFiles = Set("AsyncHttpClientFactory.scala", "AsyncHttpClientFactorySpec.scala")

  private val ForbiddenPatterns = Seq(
    "asyncHttpClient(",
    "new DefaultAsyncHttpClientConfig.Builder"
  )

  "AsyncHttpClientFactory" should "disable HTTP/2, which AHC 3.x enables by default" in {
    AsyncHttpClientFactory.config().build().isHttp2Enabled shouldBe false

    val client = AsyncHttpClientFactory.client()
    try {
      client.getConfig.isHttp2Enabled shouldBe false
    } finally {
      client.close()
    }
  }

  it should "keep the timeouts a caller sets on top of the shared config" in {
    val config = AsyncHttpClientFactory.config().setReadTimeout(java.time.Duration.ofMillis(1234)).build()
    config.isHttp2Enabled shouldBe false
    config.getReadTimeout.toMillis shouldBe 1234
  }

  it should "be the only place that constructs an AsyncHttpClient" in {
    // Kohta 1 ei huomaa uutta clienttiä, joka ohittaa tehtaan, joten tarkistetaan lähdekoodi.
    val sourceRoots = Seq("kouta-external/src", "europass-publisher/src").map(repoRoot.resolve)
    sourceRoots.foreach(root => Files.isDirectory(root) shouldBe true)

    val scalaFiles = sourceRoots.flatMap { root =>
      Files.walk(root).iterator().asScala.filter(p => p.toString.endsWith(".scala")).toList
    }
    scalaFiles.size should be > 50 // ettei tyhjä lista läpäise testiä hiljaisesti

    val violations = for {
      file <- scalaFiles
      if !AllowedFiles.contains(file.getFileName.toString)
      content = new String(Files.readAllBytes(file), "UTF-8")
      pattern <- ForbiddenPatterns
      if content.contains(pattern)
    } yield s"${repoRoot.relativize(file)}: $pattern"

    withClue(
      "Luo AHC-client AsyncHttpClientFactoryn kautta — muuten HTTP/2 jää päälle AHC 3.x:n oletuksena:\n" +
        violations.mkString("\n") + "\n"
    ) {
      violations shouldBe empty
    }
  }

  /**
   * Ei luoteta user.diriin, joka riippuu siitä ajetaanko moduulissa vai juuressa: paikannetaan
   * repon juuri test-classes-hakemistosta (<juuri>/kouta-external/target/test-classes).
   */
  private lazy val repoRoot = {
    val testClasses = new File(getClass.getProtectionDomain.getCodeSource.getLocation.toURI)
    Paths.get(testClasses.getAbsolutePath).getParent.getParent.getParent.normalize()
  }
}
