package fi.oph.kouta.koutalight

import fi.oph.kouta.domain.Kieli

import java.net.URL

package object domain {

  type KielistettyLinkki = Map[Kieli, URL]

  type Kielistetty = Map[Kieli, String]
  case class Keyword(kieli: Kieli, arvo: String)
}
