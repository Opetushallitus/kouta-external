package fi.oph.kouta.external.security

case class AuthenticationFailedException(msg: String, cause: Throwable) extends RuntimeException(msg, cause) {
  def this(msg: String) = this(msg, null)

  def this() = this(null, null)
}
