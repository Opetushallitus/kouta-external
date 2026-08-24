package fi.oph.kouta.external

import ch.qos.logback.access.jetty.RequestLogImpl
import fi.vm.sade.properties.OphProperties
import fi.oph.kouta.logging.Logging
import org.eclipse.jetty.ee10.webapp.WebAppContext
import org.eclipse.jetty.server.{RequestLog, Server}
import org.eclipse.jetty.util.resource.ResourceFactory

object JettyLauncher extends Logging {
  val DEFAULT_PORT = "8080"

  def main(args: Array[String]) {
    val port = System.getProperty("kouta-external.port", DEFAULT_PORT).toInt
    logger.info(s"Starting standalone Kouta-external Jetty on port $port...")
    new JettyLauncher(port).start().join()
  }
}

class JettyLauncher(val port: Int) {
  val server       = new Server(port)
  val context      = new WebAppContext()
  val baseResource = ResourceFactory.of(context).newClassLoaderResource("webapp")
  context.setBaseResource(baseResource)
  // Jetty 12 resolvoi suhteellisen descriptor-polun työhakemistoa vasten, ei
  // base resourcea vasten kuten Jetty 9, joten annetaan se absoluuttisena URI:na.
  context.setDescriptor(baseResource.resolve("WEB-INF/web.xml").getURI.toString)
  context.setContextPath("/kouta-external")
  server.setHandler(context)

  server.setRequestLog(requestLog(KoutaConfigurationFactory.configuration.urlProperties))

  def start(): Server = {
    server.start()
    server
  }

  private def requestLog(properties: OphProperties): RequestLog = {
    val requestLog    = new RequestLogImpl
    val logbackAccess = properties.getOrElse("logback.access", null)
    if (logbackAccess != null) {
      requestLog.setFileName(logbackAccess)
    } else {
      println("JettyLauncher: Jetty access log is printed to console, use -Dlogback.access to set configuration file")
      requestLog.setResource("/logback-access.xml")
    }
    requestLog.start()
    requestLog
  }
}
