package fi.oph.kouta.external.util

import com.sksamuel.elastic4s.Hit
import fi.oph.kouta.external.domain.indexed.OppilaitosIndexed
import org.json4s.MappingException
import org.json4s.jackson.JsonMethods.parse
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.Success

class KoutaHitReaderSpec extends AnyFlatSpec with Matchers with KoutaHitReader {

  private def hitOf(source: String): Hit = new Hit {
    override def sourceAsString: String       = source
    override def id: String                   = "1.2.246.562.10.00000000000000000099"
    override def index: String                = "oppilaitos-kouta"
    override def version: Long                = 1L
    override def seqNo: Long                  = 1L
    override def primaryTerm: Long            = 1L
    override def sort: Option[Seq[AnyRef]]    = None
    override def sourceAsMap: Map[String, AnyRef] = Map.empty
    override def exists: Boolean              = true
    override def score: Float                 = 1.0f
  }

  /** kouta-indeksoija tuottaa kenttiä muodossa "yhteystiedot": null; ne tarkoittavat samaa kuin
    * puuttuva kenttä. `yhteystiedot` on `Option[OppilaitosYhteystiedotIndexed]`, ja se luokka on
    * juuri se muoto, jonka json4s 4.x hylkää JNullista: kaikki sen kentät ovat Option-kenttiä.
    */
  private val withExplicitNulls =
    """{
      |  "oid": "1.2.246.562.10.00000000000000000099",
      |  "nimi": null,
      |  "oppilaitos": { "metadata": { "esittely": null, "yhteystiedot": null } }
      |}""".stripMargin

  "koutaHitReader" should "read a document whose optional fields are explicit nulls" in {
    val Success(oppilaitos) = koutaHitReader[OppilaitosIndexed].read(hitOf(withExplicitNulls))

    oppilaitos.oid.toString shouldBe "1.2.246.562.10.00000000000000000099"
    oppilaitos.nimi shouldBe None
    oppilaitos.oppilaitos.flatMap(_.metadata).flatMap(_.esittely) shouldBe None
    oppilaitos.oppilaitos.flatMap(_.metadata).flatMap(_.yhteystiedot) shouldBe None
  }

  it should "fix this by normalising nulls, not by relaxing the formats" in {
    // Ilman noNulls-normalisointia sama dokumentti kaatuu json4s 4.x:llä. Tämä pinnaa sen, että
    // korjaus on nullien normalisointi eikä strictOptionParsingin sammuttaminen — jälkimmäinen
    // nielaisisi myös aidot mappausvirheet Option-kenttien sisällä.
    a[MappingException] should be thrownBy parse(withExplicitNulls).extract[OppilaitosIndexed]
  }
}
