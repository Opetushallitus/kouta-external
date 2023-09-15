package fi.oph.kouta.external

import com.fasterxml.jackson.annotation.{JsonCreator, JsonProperty}
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import fi.oph.kouta.TestOids.{ChildOid, TestUserOid}
import fi.oph.kouta.domain.{Amm, Fi, Julkaistu, Sv}
import fi.oph.kouta.external.TestData.{AmmValintaperusteMetadata, Valintakoe1}
import fi.oph.kouta.external.domain.Valintaperuste
import fi.oph.kouta.external.domain.indexed.{ESResult, HakukohdeIndexed, HakukohdeIndexedTest}
import fi.vm.sade.utils.slf4j.Logging

import java.io.{BufferedReader, File, FileReader}
import java.util.UUID
import scala.io.Source

object Main  extends Logging {

  // Haluttu luokkarakenne
  case class Oid(s: String)
  case class Result(oid: Option[Oid], oid2: Option[Oid], oid3: Option[Oid])

  // ES Outputtia vastaava luokkarakenne
  case class ESResultSanteri @JsonCreator() (
                                       @JsonProperty("oid1") oid1: String,
                                       @JsonProperty("oid2") oid2: String,
                                       @JsonProperty("oid3") oid3: String) {

    def toResult(): Result = {
      Result(
        Option.apply(oid1).map(oid => Oid(oid)),
        Option.apply(oid2).map(oid => Oid(oid)),
        Option.apply(oid3).map(oid => Oid(oid))
      )
    }
  }

  def main(args: Array[String]): Unit = {
    val mapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(SerializationFeature.INDENT_OUTPUT, true)
    mapper.registerModule(DefaultScalaModule)

    try {
      val esvalue = mapper.readValue(new File("src/test/resources/elastic_dump/hakukohde-kouta-05-04-2023-at-07.37.32.840.json"), classOf[ESResult]);
      logger.info("esvalue " + esvalue )
      logger.info("esResult.toResult() = " + esvalue.toResult());
      //val testResult = esvalue.toResult()


      return 0
    } catch {
      case e: Throwable => {
        throw e
      }
    }
  }
  /*
              val bufferedSource = Source.fromFile("src/test/resources/elastic_dump/hakukohde-kouta-05-04-2023-at-07.37.32.840.json")
              for (line <- bufferedSource.getLines) {
                logger.info(line)
              }
              bufferedSource.close
             */

  //def mapper: ObjectMapper = new ObjectMapper()
  //    def json: String = "{ \"oid1\": \"abc\", \"oid2\": null}"
  //     def esResultSanteri: ESResultSanteri = mapper.readValue(json, classOf[ESResultSanteri])
  //      val result: Result = esResultSanteri.toResult()
  //   logger.info("result = " + result)
  //mapper.writeValue(new File("src/test/scala/fi/oph/kouta/external/jannetest.json"), result)

  /*
      val AmmValintaperuste: Valintaperuste = Valintaperuste(

        koulutustyyppi = Amm,
        externalId = None,
        id = None,
        tila = Julkaistu,
        hakutapaKoodiUri = Some("hakutapa_02#1"),
        kohdejoukkoKoodiUri = Some("haunkohdejoukko_17#1"),
        nimi = Map(Fi -> "nimi", Sv -> "nimi sv"),
        julkinen = false,
        valintakokeet = List(Valintakoe1),
        metadata = Some(AmmValintaperusteMetadata),
        organisaatioOid = ChildOid,
        muokkaaja = TestUserOid,
        kielivalinta = List(Fi, Sv),
        modified = None
      )

   */
  //    logger.info(mapper.writeValueAsString(AmmValintaperuste))
  //mapper.writeValue(new File("src/test/resources/elastic_dump/AmmValintaperuste.json"), AmmValintaperuste)
  //    mapper.writeValue(new File("AmmValintaperuste.json"), AmmValintaperuste)


  //JSON from file to Object
  //    val value = mapper.readValue(new File("src/test/resources/elastic_dump/hakukohde-kouta-05-04-2023-at-07.37.32.840.json"), classOf[HakukohdeIndexed]);
  //val source = scala.io.Source.fromFile("src/test/resources/elastic_dump/hakukohde-kouta-05-04-2023-at-07.37.32.840.json")
  //    val lines = try source.mkString finally source.close()

}