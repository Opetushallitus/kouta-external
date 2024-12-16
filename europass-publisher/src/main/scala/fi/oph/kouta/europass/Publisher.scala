package fi.oph.kouta.europass

import scala.concurrent.Future
import java.io.{File, BufferedWriter, FileWriter}

object Publisher {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def toteutusToFile(oid: String, dest: BufferedWriter) =
    ElasticClient.getToteutus(oid)
      .map(EuropassConversion.toteutusAsElmXml)
      .map{toteutusXml => {
        dest.write(
          <loq:Courses xsdVersion="3.1.0"
              xmlns:loq="http://data.europa.eu/snb/model/ap/loq-constraints/">
            <loq:learningOpportunityReferences>
              {toteutusXml}
            </loq:learningOpportunityReferences>
          </loq:Courses>.toString
        )
        dest.close()
      }}

  def publishedToteutuksetToFile(dest: BufferedWriter) = {
    val toteutusStream = ElasticClient.listPublished(None)
    val toteutusXmlStream = toteutusStream.map(EuropassConversion.toteutusAsElmXml)
    dest.write("<loq:Courses xsdVersion=\"3.1.0\"\n" +
      "xmlns:loq=\"http://data.europa.eu/snb/model/ap/loq-constraints/\">\n" +
      "<loq:learningOpportunityReferences>\n")
    toteutusXmlStream.foreach{toteutus => dest.write(toteutus.toString)}
    dest.write("\n</loq:learningOpportunityReferences>\n</loq:Courses>")
    dest.close()
  }

  def publishedToteutuksetAsFile(): String = {
    val tempFile = File.createTempFile("europass-export", ".xml")
    tempFile.deleteOnExit()
    val writer = new BufferedWriter(new FileWriter(tempFile))
    publishedToteutuksetToFile(writer)
    writer.close()
    tempFile.getPath()
  }

}
