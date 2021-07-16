package fi.oph.kouta.external.util

import fi.oph.kouta.domain._
import fi.oph.kouta.external.domain._
import fi.oph.kouta.external.domain.indexed._
import fi.oph.kouta.util.{GenericKoutaFormats, GenericKoutaJsonFormats}
import org.json4s.JsonAST.{JObject, JString}
import org.json4s._

import scala.util.Try

trait KoutaJsonFormats extends GenericKoutaJsonFormats with DefaultKoutaJsonFormats {
  override implicit def jsonFormats: Formats = koutaJsonFormats
}

sealed trait DefaultKoutaJsonFormats extends GenericKoutaFormats {

  def koutaJsonFormats: Formats = genericKoutaFormats ++ Seq(
    koulutusMetadataSerializer,
    koulutusMetadataIndexedSerializer,
    toteutusMetadataSerializer,
    toteutusMetadataIndexedSerializer,
    valintatapaSisaltoSerializer,
    valintaperusteMetadataSerializer,
    valintaperusteMetadataIndexedSerializer,
  )

  private def serializer[A: Manifest](deserializer: PartialFunction[JValue, A])(serializer: PartialFunction[Any, JValue]) =
    new CustomSerializer[A](_ => (deserializer, serializer))

  private def koulutusMetadataSerializer: CustomSerializer[KoulutusMetadata] = serializer[KoulutusMetadata] {
    case s: JObject =>
      implicit def formats: Formats = genericKoutaFormats

      Try(s \ "tyyppi").toOption.collect {
        case JString(tyyppi) => Koulutustyyppi.withName(tyyppi)
      }.getOrElse(Amm) match {
        case Yo => s.extract[YliopistoKoulutusMetadata]
        case Amm => s.extract[AmmatillinenKoulutusMetadata]
        case Amk => s.extract[AmmattikorkeakouluKoulutusMetadata]
        case Lk => s.extract[LukioKoulutusMetadata]
        case AmmTutkinnonOsa => s.extract[AmmatillinenTutkinnonOsaKoulutusMetadata]
        case AmmOsaamisala => s.extract[AmmatillinenOsaamisalaKoulutusMetadata]
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
        case JString(tyyppi) => Koulutustyyppi.withName(tyyppi)
      }.getOrElse(Amm) match {
        case Yo => s.extract[YliopistoKoulutusMetadataIndexed]
        case Amk => s.extract[AmmattikorkeakouluKoulutusMetadataIndexed]
        case Amm => s.extract[AmmatillinenKoulutusMetadataIndexed]
        case Lk => s.extract[LukioKoulutusMetadataIndexed]
        case AmmTutkinnonOsa => s.extract[AmmatillinenTutkinnonOsaKoulutusMetadataIndexed]
        case AmmOsaamisala => s.extract[AmmatillinenOsaamisalaKoulutusMetadataIndexed]
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
        case JString(tyyppi) => Koulutustyyppi.withName(tyyppi)
      }.getOrElse(Amm) match {
        case Yo => s.extract[YliopistoToteutusMetadata]
        case Amm => s.extract[AmmatillinenToteutusMetadata]
        case Amk => s.extract[AmmattikorkeakouluToteutusMetadata]
        case kt => throw new UnsupportedOperationException(s"Unsupported koulutustyyppi $kt")
      }
  } {
    case j: ToteutusMetadata =>
      implicit def formats: Formats = genericKoutaFormats

      Extraction.decompose(j)
  }

  private def toteutusMetadataIndexedSerializer: CustomSerializer[ToteutusMetadataIndexed] = serializer[ToteutusMetadataIndexed]{
    case s: JObject =>
      implicit def formats: Formats = genericKoutaFormats

      Try(s \ "tyyppi").toOption.collect {
        case JString(tyyppi) => Koulutustyyppi.withName(tyyppi)
      }.getOrElse(Amm) match {
        case Yo => s.extract[YliopistoToteutusMetadataIndexed]
        case Amm => s.extract[AmmatillinenToteutusMetadataIndexed]
        case Amk => s.extract[AmmattikorkeakouluToteutusMetadataIndexed]
        case kt => throw new UnsupportedOperationException(s"Unsupported koulutustyyppi $kt")
      }
  } {
    case j: ToteutusMetadataIndexed =>
      implicit def formats: Formats = genericKoutaFormats

      Extraction.decompose(j)
  }

  private def valintaperusteMetadataSerializer: CustomSerializer[ValintaperusteMetadata] = serializer[ValintaperusteMetadata] {
    case s: JObject =>
      implicit def formats: Formats = genericKoutaFormats + valintatapaSisaltoSerializer

      Try(s \ "koulutustyyppi").toOption.collect {
        case JString(tyyppi) => Koulutustyyppi.withName(tyyppi)
      }.getOrElse(Amm) match {
        case Yo => s.extract[YliopistoValintaperusteMetadata]
        case Amm => s.extract[AmmatillinenValintaperusteMetadata]
        case Amk => s.extract[AmmattikorkeakouluValintaperusteMetadata]
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
        case JString(tyyppi) => Koulutustyyppi.withName(tyyppi)
      }.getOrElse(Amm) match {
        case Yo => s.extract[YliopistoValintaperusteMetadataIndexed]
        case Amm => s.extract[AmmatillinenValintaperusteMetadataIndexed]
        case Amk => s.extract[AmmattikorkeakouluValintaperusteMetadataIndexed]
        case kt => throw new UnsupportedOperationException(s"Unsupported koulutustyyppi $kt")
      }
  } {
    case j: ValintaperusteMetadata =>
      implicit def formats: Formats = genericKoutaFormats + valintatapaSisaltoSerializer

      Extraction.decompose(j)
  }

  private def valintatapaSisaltoSerializer = new CustomSerializer[Sisalto](implicit formats => ( {
    case s: JObject =>
      Try(s \ "tyyppi").collect {
        case JString(tyyppi) if tyyppi == "teksti" =>
          Try(s \ "data").collect {
            case teksti: JObject => SisaltoTeksti(teksti.extract[Kielistetty])
          }.get
        case JString(tyyppi) if tyyppi == "taulukko" =>
          Try(s \ "data").collect {
            case taulukko: JObject => taulukko.extract[Taulukko]
          }.get
      }.get
  }, {
    case j: SisaltoTeksti =>
      implicit def formats: Formats = genericKoutaFormats

      JObject(List("tyyppi" -> JString("teksti"), "data" -> Extraction.decompose(j.teksti)))
    case j: Taulukko =>
      implicit def formats: Formats = genericKoutaFormats

      JObject(List("tyyppi" -> JString("taulukko"), "data" -> Extraction.decompose(j)))
  }))
}
