package fi.oph.kouta.europass

import com.typesafe.config.ConfigFactory

object EuropassConfiguration {
  lazy val config = ConfigFactory.load("europass-publisher")
    .withFallback(ConfigFactory.load("default"))
}
