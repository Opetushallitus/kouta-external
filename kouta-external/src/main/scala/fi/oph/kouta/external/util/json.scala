package fi.oph.kouta.external.util

import fi.oph.kouta.domain._
import fi.oph.kouta.domain.oid.HakuOid
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
    stringSerializer(HakuOid)
  )

  private def serializer[A: Manifest](deserializer: PartialFunction[JValue, A])(
      serializer: PartialFunction[Any, JValue]
  ) =
    new CustomSerializer[A](_ => (deserializer, serializer))

  private def stringSerializer[A: Manifest](construct: String => A) = serializer { case JString(s) =>
    construct(s)
  } { case a: A =>
    JString(a.toString)
  }

  private def koulutusMetadataSerializer: CustomSerializer[KoulutusMetadata] = serializer[KoulutusMetadata] {
    case s: JObject =>
      implicit def formats: Formats = genericKoutaFormats

      Try(s \ "tyyppi").toOption.collect { case JString(tyyppi) =>
        Koulutustyyppi.withName(tyyppi)
      }.getOrElse(Amm) match {
        case Yo                            => s.extract[YliopistoKoulutusMetadata]
        case Amm                           => s.extract[AmmatillinenKoulutusMetadata]
        case Amk                           => s.extract[AmmattikorkeakouluKoulutusMetadata]
        case AmmOpeErityisopeJaOpo         => s.extract[AmmOpeErityisopeJaOpoKoulutusMetadata]
        case OpePedagOpinnot               => s.extract[OpePedagOpinnotKoulutusMetadata]
        case Lk                            => s.extract[LukioKoulutusMetadata]
        case AmmTutkinnonOsa               => s.extract[AmmatillinenTutkinnonOsaKoulutusMetadata]
        case AmmOsaamisala                 => s.extract[AmmatillinenOsaamisalaKoulutusMetadata]
        case AmmMuu                        => s.extract[AmmatillinenMuuKoulutusMetadata]
        case Tuva                          => s.extract[TuvaKoulutusMetadata]
        case Telma                         => s.extract[TelmaKoulutusMetadata]
        case VapaaSivistystyoOpistovuosi   => s.extract[VapaaSivistystyoKoulutusMetadata]
        case VapaaSivistystyoMuu           => s.extract[VapaaSivistystyoKoulutusMetadata]
        case VapaaSivistystyoOsaamismerkki => s.extract[VapaaSivistystyoOsaamismerkkiKoulutusMetadata]
        case AikuistenPerusopetus          => s.extract[AikuistenPerusopetusKoulutusMetadata]
        case TaiteenPerusopetus            => s.extract[TaiteenPerusopetusKoulutusMetadata]
        case KkOpintojakso                 => s.extract[KkOpintojaksoKoulutusMetadata]
        case Erikoislaakari                => s.extract[ErikoislaakariKoulutusMetadata]
        case KkOpintokokonaisuus           => s.extract[KkOpintokokonaisuusKoulutusMetadata]
        case Erikoistumiskoulutus          => s.extract[ErikoistumiskoulutusMetadata]
        case Muu                           => s.extract[MuuKoulutusMetadata]
        case kt                            => throw new UnsupportedOperationException(s"Unsupported koulutustyyppi $kt")
      }
  } { case j: KoulutusMetadata =>
    implicit def formats: Formats = genericKoutaFormats

    Extraction.decompose(j)
  }

  private def koulutusMetadataIndexedSerializer: CustomSerializer[KoulutusMetadataIndexed] =
    serializer[KoulutusMetadataIndexed] { case s: JObject =>
      implicit def formats: Formats = genericKoutaFormats

      Try(s \ "tyyppi").toOption.collect { case JString(tyyppi) =>
        Koulutustyyppi.withName(tyyppi)
      }.getOrElse(Amm) match {
        case Yo                          => s.extract[YliopistoKoulutusMetadataIndexed]
        case Amk                         => s.extract[AmmattikorkeakouluKoulutusMetadataIndexed]
        case AmmOpeErityisopeJaOpo       => s.extract[AmmOpeErityisopeJaOpoKoulutusMetadataIndexed]
        case OpePedagOpinnot             => s.extract[OpePedagOpinnotKoulutusMetadataIndexed]
        case Amm                         => s.extract[AmmatillinenKoulutusMetadataIndexed]
        case Lk                          => s.extract[LukioKoulutusMetadataIndexed]
        case AmmTutkinnonOsa             => s.extract[AmmatillinenTutkinnonOsaKoulutusMetadataIndexed]
        case AmmOsaamisala               => s.extract[AmmatillinenOsaamisalaKoulutusMetadataIndexed]
        case AmmMuu                      => s.extract[AmmatillinenMuuKoulutusMetadataIndexed]
        case Tuva                        => s.extract[TuvaKoulutusMetadataIndexed]
        case Telma                       => s.extract[TelmaKoulutusMetadataIndexed]
        case VapaaSivistystyoOpistovuosi => s.extract[VapaaSivistystyoKoulutusMetadataIndexed]
        case VapaaSivistystyoMuu         => s.extract[VapaaSivistystyoKoulutusMetadataIndexed]
        case VapaaSivistystyoOsaamismerkki => s.extract[VapaaSivistystyoOsaamismerkkiKoulutusMetadataIndexed]
        case AikuistenPerusopetus        => s.extract[AikuistenPerusopetusKoulutusMetadataIndexed]
        case TaiteenPerusopetus          => s.extract[TaiteenPerusopetusKoulutusMetadataIndexed]
        case KkOpintojakso               => s.extract[KkOpintojaksoKoulutusMetadataIndexed]
        case Erikoislaakari              => s.extract[ErikoislaakariKoulutusMetadataIndexed]
        case KkOpintokokonaisuus         => s.extract[KkOpintokokonaisuusKoulutusMetadataIndexed]
        case Erikoistumiskoulutus        => s.extract[ErikoistumiskoulutusMetadataIndexed]
        case Muu                         => s.extract[MuuKoulutusMetadataIndexed]
        case kt                          => throw new UnsupportedOperationException(s"Unsupported koulutustyyppi $kt")
      }
    } { case j: KoulutusMetadataIndexed =>
      implicit def formats: Formats = genericKoutaFormats

      Extraction.decompose(j)
    }

  private def toteutusMetadataSerializer: CustomSerializer[ToteutusMetadata] = serializer[ToteutusMetadata] {
    case s: JObject =>
      implicit def formats: Formats = genericKoutaFormats

      Try(s \ "tyyppi").toOption.collect { case JString(tyyppi) =>
        Koulutustyyppi.withName(tyyppi)
      }.getOrElse(Amm) match {
        case Yo                            => s.extract[YliopistoToteutusMetadata]
        case Amk                           => s.extract[AmmattikorkeakouluToteutusMetadata]
        case AmmOpeErityisopeJaOpo         => s.extract[AmmOpeErityisopeJaOpoToteutusMetadata]
        case OpePedagOpinnot               => s.extract[OpePedagOpinnotToteutusMetadata]
        case Amm                           => s.extract[AmmatillinenToteutusMetadata]
        case Lk                            => s.extract[LukioToteutusMetadata]
        case AmmTutkinnonOsa               => s.extract[AmmatillinenTutkinnonOsaToteutusMetadata]
        case AmmOsaamisala                 => s.extract[AmmatillinenOsaamisalaToteutusMetadata]
        case AmmMuu                        => s.extract[AmmatillinenMuuToteutusMetadata]
        case Tuva                          => s.extract[TuvaToteutusMetadata]
        case Telma                         => s.extract[TelmaToteutusMetadata]
        case VapaaSivistystyoOpistovuosi   => s.extract[VapaaSivistystyoOpistovuosiToteutusMetadata]
        case VapaaSivistystyoMuu           => s.extract[VapaaSivistystyoMuuToteutusMetadata]
        case VapaaSivistystyoOsaamismerkki => s.extract[VapaaSivistystyoOsaamismerkkiToteutusMetadata]
        case AikuistenPerusopetus          => s.extract[AikuistenPerusopetusToteutusMetadata]
        case TaiteenPerusopetus            => s.extract[TaiteenPerusopetusToteutusMetadata]
        case KkOpintojakso                 => s.extract[KkOpintojaksoToteutusMetadata]
        case Erikoislaakari                => s.extract[ErikoislaakariToteutusMetadata]
        case KkOpintokokonaisuus           => s.extract[KkOpintokokonaisuusToteutusMetadata]
        case Erikoistumiskoulutus          => s.extract[ErikoistumiskoulutusToteutusMetadata]
        case Muu                           => s.extract[MuuToteutusMetadata]
        case kt                            => throw new UnsupportedOperationException(s"Unsupported koulutustyyppi $kt")
      }
  } { case j: ToteutusMetadata =>
    implicit def formats: Formats = genericKoutaFormats

    Extraction.decompose(j)
  }

  private def toteutusMetadataIndexedSerializer: CustomSerializer[ToteutusMetadataIndexed] =
    serializer[ToteutusMetadataIndexed] { case s: JObject =>
      implicit def formats: Formats = genericKoutaFormats

      Try(s \ "tyyppi").toOption.collect { case JString(tyyppi) =>
        Koulutustyyppi.withName(tyyppi)
      }.getOrElse(Amm) match {
        case Yo                            => s.extract[YliopistoToteutusMetadataIndexed]
        case Amk                           => s.extract[AmmattikorkeakouluToteutusMetadataIndexed]
        case AmmOpeErityisopeJaOpo         => s.extract[AmmOpeErityisopeJaOpoToteutusMetadataIndexed]
        case OpePedagOpinnot               => s.extract[OpePedagOpinnotToteutusMetadataIndexed]
        case Amm                           => s.extract[AmmatillinenToteutusMetadataIndexed]
        case Lk                            => s.extract[LukioToteutusMetadataIndexed]
        case AmmTutkinnonOsa               => s.extract[AmmatillinenTutkinnonOsaToteutusMetadataIndexed]
        case AmmOsaamisala                 => s.extract[AmmatillinenOsaamisalaToteutusMetadataIndexed]
        case AmmMuu                        => s.extract[AmmatillinenMuuToteutusMetadataIndexed]
        case Tuva                          => s.extract[TuvaToteutusMetadataIndexed]
        case Telma                         => s.extract[TelmaToteutusMetadataIndexed]
        case VapaaSivistystyoOpistovuosi   => s.extract[VapaaSivistystyoOpistovuosiToteutusMetadataIndexed]
        case VapaaSivistystyoMuu           => s.extract[VapaaSivistystyoMuuToteutusMetadataIndexed]
        case VapaaSivistystyoOsaamismerkki => s.extract[VapaaSivistystyoOsaamismerkkiToteutusMetadataIndexed]
        case AikuistenPerusopetus          => s.extract[AikuistenPerusopetusToteutusMetadataIndexed]
        case TaiteenPerusopetus            => s.extract[TaiteenPerusopetusToteutusMetadataIndexed]
        case KkOpintojakso                 => s.extract[KkOpintojaksoToteutusMetadataIndexed]
        case Erikoislaakari                => s.extract[ErikoislaakariToteutusMetadataIndexed]
        case KkOpintokokonaisuus           => s.extract[KkOpintokokonaisuusToteutusMetadataIndexed]
        case Erikoistumiskoulutus          => s.extract[ErikoistumiskoulutusToteutusMetadataIndexed]
        case Muu                           => s.extract[MuuToteutusMetadataIndexed]
        case kt                            => throw new UnsupportedOperationException(s"Unsupported koulutustyyppi $kt")
      }
    } { case j: ToteutusMetadataIndexed =>
      implicit def formats: Formats = genericKoutaFormats

      Extraction.decompose(j)
    }

  private def valintaperusteMetadataSerializer: CustomSerializer[ValintaperusteMetadata] =
    serializer[ValintaperusteMetadata] { case s: JObject =>
      implicit def formats: Formats = genericKoutaFormats + valintatapaSisaltoSerializer

      Try(s \ "tyyppi").toOption.collect { case JString(tyyppi) =>
        Koulutustyyppi.withName(tyyppi)
      }.getOrElse(Amm) match {
        case kt if Koulutustyyppi.values contains kt => s.extract[GenericValintaperusteMetadata]
        case kt                                      => throw new UnsupportedOperationException(s"Unsupported koulutustyyppi $kt")
      }
    } { case j: ValintaperusteMetadata =>
      implicit def formats: Formats = genericKoutaFormats + valintatapaSisaltoSerializer

      Extraction.decompose(j)
    }

  private def valintaperusteMetadataIndexedSerializer: CustomSerializer[ValintaperusteMetadataIndexed] =
    serializer[ValintaperusteMetadataIndexed] { case s: JObject =>
      implicit def formats: Formats = genericKoutaFormats + valintatapaSisaltoSerializer

      Try(s \ "tyyppi").toOption.collect { case JString(tyyppi) =>
        Koulutustyyppi.withName(tyyppi)
      }.getOrElse(Amm) match {
        case kt if Koulutustyyppi.values contains kt =>
          s.extract[GenericValintaperusteMetadataIndexed]
        case kt => throw new UnsupportedOperationException(s"Unsupported koulutustyyppi $kt")
      }
    } { case j: ValintaperusteMetadata =>
      implicit def formats: Formats = genericKoutaFormats + valintatapaSisaltoSerializer

      Extraction.decompose(j)
    }

  private def valintatapaSisaltoSerializer = new CustomSerializer[Sisalto](implicit formats =>
    (
      { case s: JObject =>
        Try(s \ "tyyppi").collect {
          case JString(tyyppi) if tyyppi == "teksti" =>
            Try(s \ "data").collect { case teksti: JObject =>
              SisaltoTeksti(teksti.extract[Kielistetty])
            }.get
          case JString(tyyppi) if tyyppi == "taulukko" =>
            Try(s \ "data").collect { case taulukko: JObject =>
              taulukko.extract[Taulukko]
            }.get
        }.get
      },
      {
        case j: SisaltoTeksti =>
          implicit def formats: Formats = genericKoutaFormats

          JObject(List("tyyppi" -> JString("teksti"), "data" -> Extraction.decompose(j.teksti)))
        case j: Taulukko =>
          implicit def formats: Formats = genericKoutaFormats

          JObject(List("tyyppi" -> JString("taulukko"), "data" -> Extraction.decompose(j)))
      }
    )
  )
}
