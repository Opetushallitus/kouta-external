package fi.oph.kouta.external.elasticsearch

import com.sksamuel.elastic4s.ElasticError

case class ElasticSearchException(error: ElasticError) extends Exception
