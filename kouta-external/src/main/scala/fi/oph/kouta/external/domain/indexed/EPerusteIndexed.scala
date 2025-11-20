package fi.oph.kouta.external.domain.indexed

import fi.oph.kouta.domain.oid.GenericOid
import fi.oph.kouta.domain.{En, Fi, Sv}
import fi.oph.kouta.external.domain.Kielistetty

case class KielistettyEPerusteField(_id: String, fi: Option[String], en: Option[String], sv: Option[String])

case class EPerusteIndexed(
    oid: GenericOid,
    tyotehtavatJoissaVoiToimia: KielistettyEPerusteField,
    suorittaneenOsaaminen: KielistettyEPerusteField
) {

  private def toKielistetty(eperusteKielistetty: KielistettyEPerusteField): Kielistetty = {
    Map(Fi -> eperusteKielistetty.fi, Sv -> eperusteKielistetty.sv, En -> eperusteKielistetty.en).collect {
      case (kieli, Some(value)) => kieli -> value
    }
  }

  def toEPeruste: EPeruste = {
    val tyotehtavatJoissaVoiToimia = this.tyotehtavatJoissaVoiToimia
    val suorittaneenOsaaminen      = this.suorittaneenOsaaminen

    EPeruste(
      oid = oid,
      tyotehtavatJoissaVoiToimia = toKielistetty(tyotehtavatJoissaVoiToimia),
      suorittaneenOsaaminen = toKielistetty(suorittaneenOsaaminen)
    )
  }
}

case class EPeruste(oid: GenericOid, tyotehtavatJoissaVoiToimia: Kielistetty, suorittaneenOsaaminen: Kielistetty)
