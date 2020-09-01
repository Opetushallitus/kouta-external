package fi.oph.kouta.external.integration.fixture

import java.util.UUID

import clojure.java.api.Clojure
import fi.oph.kouta.external.TempElasticClient
import fi.oph.kouta.domain.oid._

trait IndeksoijaFixture {
  private val require = Clojure.`var`("clojure.core", "require")

  private val elasticUtils = "clj-elasticsearch.elastic-utils"
  require.invoke(Clojure.read(elasticUtils))

  private val indexerFixture = "kouta-indeksoija-service.fixture.kouta-indexer-fixture"
  require.invoke(Clojure.read(indexerFixture))

  private val _indexAll           = Clojure.`var`(indexerFixture, "index-all")
  private val indexWithoutRelated = Clojure.`var`(indexerFixture, "index-oids-without-related-indices")
  private val teardown            = Clojure.`var`(indexerFixture, "teardown")
  private val init                = Clojure.`var`(indexerFixture, "init")

  private val intern = Clojure.`var`("clojure.core", "intern")
  intern.invoke(
    Clojure.read("clj-elasticsearch.elastic-utils"),
    Clojure.read("elastic-host"),
    Clojure.read(TempElasticClient.url)
  )

  def initIndices(): Unit = {
    init.invoke()
  }

  def resetIndices(): Unit = {
    teardown.invoke()
  }

  def indexAll(): Unit = _indexAll.invoke()

  def indexKoulutus(koulutusOid: KoulutusOid): Unit = {
    indexWithoutRelated.invoke(Clojure.read(s"{ :koulutukset [${'"'}${koulutusOid.s}${'"'}]}"))
  }

  def indexToteutus(toteutusOid: ToteutusOid): Unit = {
    indexWithoutRelated.invoke(Clojure.read(s"{ :toteutukset [${'"'}${toteutusOid.s}${'"'}]}"))
  }

  def indexHaku(hakuOid: HakuOid): Unit = {
    indexWithoutRelated.invoke(Clojure.read(s"{ :haut [${'"'}${hakuOid.s}${'"'}]}"))
  }

  def indexHakukohde(hakukohdeOid: HakukohdeOid): Unit = {
    indexWithoutRelated.invoke(Clojure.read(s"{ :hakukohteet [${'"'}${hakukohdeOid.s}${'"'}]}"))
  }

  def indexValintaperuste(id: UUID): Unit = {
    indexWithoutRelated.invoke(Clojure.read(s"{ :valintaperusteet [${'"'}${id.toString}${'"'}]}"))
  }

  def indexSorakuvaus(id: UUID): Unit = {
    indexWithoutRelated.invoke(Clojure.read(s"{ :sorakuvaukset [${'"'}${id.toString}${'"'}]}"))
  }
}
