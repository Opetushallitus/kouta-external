package fi.oph.kouta.external.domain.enums

sealed abstract class Operation(override val name: String) extends BasicType

object Operation extends BasicTypeCompanion[Operation] {
  case object Update extends Operation("UPDATE")

  case object Create extends Operation("CREATE")

  case object Upsert extends Operation("CREATE OR UPDATE")

  override def all: List[Operation] = List(Update, Create, Upsert)
}
