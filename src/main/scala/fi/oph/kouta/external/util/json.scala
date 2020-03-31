package fi.oph.kouta.external.util

import java.net.InetAddress
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

import fi.oph.kouta.external.domain._
import fi.oph.kouta.external.domain.enums._
import fi.oph.kouta.external.domain.indexed._
import fi.oph.kouta.external.domain.oid._
import fi.oph.kouta.external.security.ServiceTicket
import org.json4s.JsonAST.{JObject, JString}
import org.json4s._
import org.json4s.jackson.Serialization.write

import scala.util.Try

trait KoutaJsonFormats extends DefaultKoutaJsonFormats {

  implicit def jsonFormats: Formats = koutaJsonFormats

  def toJson(data: AnyRef): String = write(data)
}

sealed trait DefaultKoutaJsonFormats {

  val ISO_LOCAL_DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

  def koutaJsonFormats: Formats = genericKoutaFormats ++ Seq(
    koulutusMetadataSerializer,
    koulutusMetadataIndexedSerializer,
    toteutusMetadataSerializer,
    valintatapaSisaltoSerializer,
    valintaperusteMetadataSerializer,
    valintaperusteMetadataIndexedSerializer,
  )

  private def genericKoutaFormats: Formats = DefaultFormats
    .addKeySerializers(Seq(kieliKeySerializer)) ++
    Seq(
      localDateTimeSerializer,
      stringSerializer(Julkaisutila.apply),
      stringSerializer(Koulutustyyppi.apply),
      stringSerializer(Hakulomaketyyppi.apply),
      stringSerializer(Kieli.apply),
      stringSerializer(UUID.fromString),
      stringSerializer(LiitteenToimitustapa.apply),
      stringSerializer(HakuOid),
      stringSerializer(HakukohdeOid),
      stringSerializer(KoulutusOid),
      stringSerializer(ToteutusOid),
      stringSerializer(OrganisaatioOid),
      stringSerializer(UserOid),
      stringSerializer(GenericOid),
      stringSerializer(InetAddress.getByName, (ip: InetAddress) => ip.getHostAddress),
      stringSerializer(ServiceTicket.apply),
    )

  private def serializer[A: Manifest](deserializer: PartialFunction[JValue, A])(serializer: PartialFunction[Any, JValue]) =
    new CustomSerializer[A](_ => (deserializer, serializer))

  private def keySerializer[A: Manifest](deserializer: PartialFunction[String, A])(serializer: PartialFunction[Any, String]) =
    new CustomKeySerializer[A](_ => (deserializer, serializer))

  private def kieliKeySerializer: CustomKeySerializer[Kieli] = keySerializer {
    case s: String => Kieli(s)
  } {
    case k: Kieli => k.toString
  }

  private def localDateTimeSerializer: CustomSerializer[LocalDateTime] = serializer {
    case JString(i) => LocalDateTime.from(ISO_LOCAL_DATE_TIME_FORMATTER.parse(i))
  } {
    case i: LocalDateTime => JString(ISO_LOCAL_DATE_TIME_FORMATTER.format(i))
  }

  private def stringSerializer[A: Manifest](construct: String => A): CustomSerializer[A] =
    stringSerializer(construct, (a: A) => a.toString)

  private def stringSerializer[A: Manifest](construct: String => A, deconstruct: A => String): CustomSerializer[A] =
    serializer {
      case JString(s) => construct(s)
    } {
      case a: A => JString(deconstruct(a))
    }

  private def koulutusMetadataSerializer: CustomSerializer[KoulutusMetadata] = serializer[KoulutusMetadata] {
    case s: JObject =>
      implicit def formats: Formats = genericKoutaFormats

      Try(s \ "tyyppi").toOption.collect {
        case JString(tyyppi) => Koulutustyyppi(tyyppi)
      }.getOrElse(Koulutustyyppi.Amm) match {
        case Koulutustyyppi.Yo => s.extract[YliopistoKoulutusMetadata]
        case Koulutustyyppi.Amm => s.extract[AmmatillinenKoulutusMetadata]
        case Koulutustyyppi.Amk => s.extract[AmmattikorkeakouluKoulutusMetadata]
        case kt => throw new UnsupportedOperationException(s"Unsupported koulutustyyppi $kt")
      }
  } {
    case j: KoulutusMetadata =>
      implicit def formats: Formats = genericKoutaFormats

      Extraction.decompose(j)
  }

  private def koulutusMetadataIndexedSerializer: CustomSerializer[KoulutusMetadataIndexed] = serializer[KoulutusMetadataIndexed] {
    case s: JObject =>
      implicit def formats: Formats = genericKoutaFormats

      Try(s \ "tyyppi").toOption.collect {
        case JString(tyyppi) => Koulutustyyppi(tyyppi)
      }.getOrElse(Koulutustyyppi.Amm) match {
        case Koulutustyyppi.Yo => s.extract[YliopistoKoulutusMetadataIndexed]
        case Koulutustyyppi.Amk => s.extract[AmmattikorkeakouluKoulutusMetadataIndexed]
        case Koulutustyyppi.Amm => s.extract[AmmatillinenKoulutusMetadataIndexed]
        case kt => throw new UnsupportedOperationException(s"Unsupported koulutustyyppi $kt")
      }
  } {
    case j: KoulutusMetadataIndexed =>
      implicit def formats: Formats = genericKoutaFormats

      Extraction.decompose(j)
  }

  private def toteutusMetadataSerializer: CustomSerializer[ToteutusMetadata] = serializer[ToteutusMetadata]{
    case s: JObject =>
      implicit def formats: Formats = genericKoutaFormats

      Try(s \ "tyyppi").toOption.collect {
        case JString(tyyppi) => Koulutustyyppi(tyyppi)
      }.getOrElse(Koulutustyyppi.Amm) match {
        case Koulutustyyppi.Yo => s.extract[YliopistoToteutusMetadata]
        case Koulutustyyppi.Amm => s.extract[AmmatillinenToteutusMetadata]
        case Koulutustyyppi.Amk => s.extract[AmmattikorkeakouluToteutusMetadata]
        case kt => throw new UnsupportedOperationException(s"Unsupported koulutustyyppi $kt")
      }
  } {
    case j: ToteutusMetadata =>
      implicit def formats: Formats = genericKoutaFormats

      Extraction.decompose(j)
  }

  private def valintaperusteMetadataSerializer: CustomSerializer[ValintaperusteMetadata] = serializer[ValintaperusteMetadata] {
    case s: JObject =>
      implicit def formats: Formats = genericKoutaFormats + valintatapaSisaltoSerializer

      Try(s \ "koulutustyyppi").toOption.collect {
        case JString(tyyppi) => Koulutustyyppi(tyyppi)
      }.getOrElse(Koulutustyyppi.Amm) match {
        case Koulutustyyppi.Yo => s.extract[YliopistoValintaperusteMetadata]
        case Koulutustyyppi.Amm => s.extract[AmmatillinenValintaperusteMetadata]
        case Koulutustyyppi.Amk => s.extract[AmmattikorkeakouluValintaperusteMetadata]
        case kt => throw new UnsupportedOperationException(s"Unsupported koulutustyyppi $kt")
      }
  } {
    case j: ValintaperusteMetadata =>
      implicit def formats: Formats = genericKoutaFormats + valintatapaSisaltoSerializer

      Extraction.decompose(j)
  }

  private def valintaperusteMetadataIndexedSerializer: CustomSerializer[ValintaperusteMetadataIndexed] = serializer[ValintaperusteMetadataIndexed]{
    case s: JObject =>
      implicit def formats: Formats = genericKoutaFormats + valintatapaSisaltoSerializer

      Try(s \ "tyyppi").toOption.collect {
        case JString(tyyppi) => Koulutustyyppi(tyyppi)
      }.getOrElse(Koulutustyyppi.Amm) match {
        case Koulutustyyppi.Yo => s.extract[YliopistoValintaperusteMetadataIndexed]
        case Koulutustyyppi.Amm => s.extract[AmmatillinenValintaperusteMetadataIndexed]
        case Koulutustyyppi.Amk => s.extract[AmmattikorkeakouluValintaperusteMetadataIndexed]
        case kt => throw new UnsupportedOperationException(s"Unsupported koulutustyyppi $kt")
      }
  } {
    case j: ValintaperusteMetadata =>
      implicit def formats: Formats = genericKoutaFormats + valintatapaSisaltoSerializer

      Extraction.decompose(j)
  }

  private def valintatapaSisaltoSerializer = new CustomSerializer[ValintatapaSisalto](implicit formats => ( {
    case s: JObject =>
      Try(s \ "tyyppi").collect {
        case JString(tyyppi) if tyyppi == "teksti" =>
          Try(s \ "data").collect {
            case teksti: JObject => ValintatapaSisaltoTeksti(teksti.extract[Kielistetty])
          }.get
        case JString(tyyppi) if tyyppi == "taulukko" =>
          Try(s \ "data").collect {
            case taulukko: JObject => taulukko.extract[Taulukko]
          }.get
      }.get
  }, {
    case j: ValintatapaSisaltoTeksti =>
      implicit def formats: Formats = genericKoutaFormats

      JObject(List("tyyppi" -> JString("teksti"), "data" -> Extraction.decompose(j.teksti)))
    case j: Taulukko =>
      implicit def formats: Formats = genericKoutaFormats

      JObject(List("tyyppi" -> JString("taulukko"), "data" -> Extraction.decompose(j)))
  }))
}
