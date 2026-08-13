package fi.oph.kouta.external.client

import org.asynchttpclient.{AsyncHttpClient, DefaultAsyncHttpClientConfig, Dsl}

/**
 * AHC 3.x asettaa http2Enabled-oletukseksi true, eikä sitä voi kääntää pois system
 * propertyllä eikä ahc-default.propertiesilla — vain setHttp2Enabled(false):lla. AHC 2.x:ssä
 * HTTP/2:ta ei ollut lainkaan, joten tämä pitää päivityksen transport-tasolla neutraalina.
 * java-cas tekee saman CasClientBuilderissaan.
 *
 * Kaikki AHC-instanssit on luotava tämän kautta; AsyncHttpClientFactorySpec valvoo sitä.
 */
object AsyncHttpClientFactory {
  def config(): DefaultAsyncHttpClientConfig.Builder = Dsl.config().setHttp2Enabled(false)

  def client(): AsyncHttpClient = client(config())

  def client(builder: DefaultAsyncHttpClientConfig.Builder): AsyncHttpClient = Dsl.asyncHttpClient(builder)
}
