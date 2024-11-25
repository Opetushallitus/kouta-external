package fi.oph.kouta.europass

import fi.vm.sade.utils.slf4j.Logging
import com.typesafe.config.{Config, ConfigFactory}

object EuropassConfiguration extends Logging {
  def classPathUrls(cl: ClassLoader): Array[java.net.URL] = cl match {
    case null => Array()
    case u: java.net.URLClassLoader => u.getURLs() ++ classPathUrls(cl.getParent)
    case _ => classPathUrls(cl.getParent)
  }

  def createConfig(): Config = {
    logger.info("Reading configuration from classpath: " +
      classPathUrls(getClass.getClassLoader).mkString(", "))

    val templatedConfig = ConfigFactory.load("kouta-external-europass")
    logger.info(s"Templated config is $templatedConfig")
    val localConfig = ConfigFactory.load("europass-publisher")
    logger.info(s"Local config is $localConfig")
    val fallbackConfig = ConfigFactory.load("default")
    logger.info(s"Fallback config is $fallbackConfig")
    return templatedConfig.withFallback(localConfig).withFallback(fallbackConfig)
  }

  lazy val config = createConfig()
}
