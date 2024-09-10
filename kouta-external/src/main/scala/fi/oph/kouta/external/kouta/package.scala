package fi.oph.kouta.external

package object kouta {
  type KoutaResponse[T] = Either[(Int, String), T]
}
