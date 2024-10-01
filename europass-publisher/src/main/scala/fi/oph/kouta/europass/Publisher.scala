package fi.oph.kouta.europass

import java.io.BufferedWriter

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

}
