package fi.oph.kouta.europass

import fi.vm.sade.utils.slf4j.Logging
import com.typesafe.config.{Config, ConfigFactory}
import java.io.File

object EuropassConfiguration extends Logging {
  def classPathUrls(cl: ClassLoader): Array[java.net.URL] = cl match {
    case null => Array()
    case u: java.net.URLClassLoader => u.getURLs() ++ classPathUrls(cl.getParent)
    case _ => classPathUrls(cl.getParent)
  }

  def createConfig(): Config = {
    logger.info("Reading configuration from classpath: " +
      classPathUrls(getClass.getClassLoader).mkString(", "))

    val configFile = new File(System.getProperty("user.home") +
      "/oph-configuration/kouta-external-europass.properties")
    val templatedConfig = if (configFile.exists()) {
      ConfigFactory.load(ConfigFactory.parseFile(configFile))
    } else {
      logger.warn("Actual config file kouta-external-europass.properties not found")
      ConfigFactory.load()
    }
    val localConfig = ConfigFactory.load("europass-publisher")
    val fallbackConfig = ConfigFactory.load("default")
    return templatedConfig.withFallback(localConfig).withFallback(fallbackConfig)
  }

  lazy val config = createConfig()
}
