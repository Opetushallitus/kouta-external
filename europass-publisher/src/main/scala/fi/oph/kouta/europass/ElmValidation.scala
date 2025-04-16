package fi.oph.kouta.europass

import scala.io.Source
import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.{Schema, SchemaFactory, Validator}
// import org.xml.sax.{ErrorHandler, SAXParseException}

object ElmValidation {

  val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
  val loqSchemaURI = getClass().getClassLoader().getResource("xsd/loq.xsd")
  val loqSchema: Schema = schemaFactory.newSchema(loqSchemaURI)
  val loqValidator: Validator = loqSchema.newValidator()

  def validateXml(xmlFile: String): Boolean = {
    loqValidator.validate(new StreamSource(Source.fromFile(xmlFile).bufferedReader))
    return true
  }

}
