package fi.oph.kouta.europass

import fi.vm.sade.utils.slf4j.Logging
import com.typesafe.config.ConfigFactory

object EuropassConfiguration extends Logging {
  def classPathUrls(cl: ClassLoader): Array[java.net.URL] = cl match {
    case null => Array()
    case u: java.net.URLClassLoader => u.getURLs() ++ classPathUrls(cl.getParent)
    case _ => classPathUrls(cl.getParent)
  }

  logger.info("Reading configuration from classpath:")
  logger.info(classPathUrls(getClass.getClassLoader).mkString(", "))

  val templatedConfig = ConfigFactory.load("kouta-external-europass")
  //val templatedConfigEP = templatedConfig.getConfig("europass-publisher")
  logger.info(s"Templated config is $templatedConfig")
  val localConfig = ConfigFactory.load("europass-publisher")
  //val localConfigEP = localConfig.getConfig("europass-publisher")
  logger.info(s"Local config is $localConfig")
  val fallbackConfig = ConfigFactory.load("default")
  //val fallbackConfigEP = fallbackConfig.getConfig("europass-publisher")
  logger.info(s"Fallback config is $fallbackConfig")
  lazy val config = templatedConfig.withFallback(localConfig).withFallback(fallbackConfig)
}
