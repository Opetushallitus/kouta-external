package fi.oph.kouta.external.client

import fi.oph.kouta.client.HttpClient
import fi.oph.kouta.external.KoutaConfigurationFactory
import fi.oph.kouta.external.kouta.CallerId
import fi.oph.kouta.external.security._
import fi.oph.kouta.security.Authority
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.jackson.JsonMethods.parse

object KayttooikeusClient extends KayttooikeusClient

trait KayttooikeusClient extends HttpClient with CallerId with Logging {

  import org.json4s._

  private implicit val formats   = DefaultFormats
  private lazy val urlProperties = KoutaConfigurationFactory.configuration.urlProperties

  def getUserByUsername(username: String): KayttooikeusUserDetails = {
    val url = urlProperties.url(s"kayttooikeus-service.userDetails.byUsername", username)

    val errorHandler = (_: String, status: Int, response: String) =>
      status match {
        case 404 =>
          throw new AuthenticationFailedException(
            s"User not found with username: $username, got response $status $response"
          )
        case _ =>
          throw new RuntimeException(s"Failed to get username $username details using URL $url, got response $status $response")
      }

    get(url, errorHandler, followRedirects = true) { response =>
      val kayttooikeusDto = parse(response).extract[KayttooikeusUserResp]
      KayttooikeusUserDetails(
        kayttooikeusDto.authorities
          .map(a => Authority(a.authority.replace("ROLE_", "")))
          .toSet,
        kayttooikeusDto.username
      )
    }
  }
}

case class KayttooikeusUserResp(authorities: List[GrantedAuthority], username: String)

case class GrantedAuthority(authority: String)
