package fi.oph.kouta.europass.test

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.scalatra.test.scalatest.ScalatraFlatSpec
import scala.io.Source
import java.io.{File, BufferedWriter, FileWriter}

import fi.oph.kouta.europass.OrganisationClient

object TestOrganisationClient extends OrganisationClient {
  override def getOrganisation(oid: String): JValue = {
    try {
      parse(Source.fromResource(s"organisaatio-$oid.json").bufferedReader)
    } catch {
      case e: NullPointerException => JObject()
    }
  }

  override def getOrganisationCsv(): String = {
    val tempFile = File.createTempFile("organisations", ".csv")
    tempFile.deleteOnExit()
    val writer = new BufferedWriter(new FileWriter(tempFile))
    writer.write(Source.fromResource("organisaatio.csv").mkString)
    writer.close()
    tempFile.getPath()
  }
}
