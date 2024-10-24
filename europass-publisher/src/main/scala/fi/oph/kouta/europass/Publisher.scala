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

  def publishedToteutuksetToFile(dest: BufferedWriter) =
    ElasticClient.listPublished(None)
      .map{toteutukset => toteutukset.toList.map(EuropassConversion.toteutusAsElmXml)}
      .map{toteutusXmlList => {
        dest.write(
          <loq:Courses xsdVersion="3.1.0"
              xmlns:loq="http://data.europa.eu/snb/model/ap/loq-constraints/">
            <loq:learningOpportunityReferences>
              {toteutusXmlList}
            </loq:learningOpportunityReferences>
          </loq:Courses>.toString
        )
        dest.close()
      }}

  def publishedToteutuksetAsFile(): Future[String] = {
    val tempFile = File.createTempFile("europass-export", ".xml")
    tempFile.deleteOnExit()
    val writer = new BufferedWriter(new FileWriter(tempFile))
    publishedToteutuksetToFile(writer)
      .map{_ => {
        writer.close()
        tempFile.getPath()
      }}
  }

}
