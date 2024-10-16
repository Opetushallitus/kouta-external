package fi.oph.kouta.external

object CommandLine {

  import scala.sys.process.stringToProcess

  def run(command: String) = command.run()


  def runBlocking(command: String, failOnError: Boolean = true): Int = {
    val returnValue = command.!
    if (failOnError && returnValue != 0) {
      throw new RuntimeException(s"Command '$command' exited with $returnValue")
    }
    returnValue
  }
}
