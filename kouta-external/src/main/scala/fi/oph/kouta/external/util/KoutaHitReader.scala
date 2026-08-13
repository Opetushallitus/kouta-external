package fi.oph.kouta.external.util

import com.sksamuel.elastic4s.{Hit, HitReader}
import org.json4s._
import org.json4s.jackson.JsonMethods.parse

import scala.util.Try

/** Korvaa elastic4s:n `Json4sHitReader`in muuten samanlaisella lukijalla, joka normalisoi
 * dokumentin eksplisiittiset nullit puuttuviksi kentiksi ennen purkamista.
 *
 * kouta-indeksoijan tuottamissa dokumenteissa `"kenttä": null` tarkoittaa samaa kuin puuttuva
 * kenttä, ja json4s 3.6:ssa se myös purkautui `None`:ksi. json4s 4.x hylkää JNullin luokalle,
 * jolla on Option-kenttiä (`Extraction$ClassInstanceBuilder.result()`), joten ilman tätä
 * `MappingException: No value set for Option property: ...` kaataa luvun. Vastaava sanitointi on
 * kouta-backendissa `KoutaServlet.parsedBody`issa.
 */
trait KoutaHitReader extends KoutaJsonFormats {
  implicit def koutaHitReader[T: Manifest]: HitReader[T] = new HitReader[T] {
    override def read(hit: Hit): Try[T] = Try(parse(hit.sourceAsString).noNulls.extract[T])
  }
}
