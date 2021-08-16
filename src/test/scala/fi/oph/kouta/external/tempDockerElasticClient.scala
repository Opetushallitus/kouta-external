package fi.oph.kouta.external

import com.sksamuel.elastic4s.http.{ElasticClient, ElasticProperties}
import fi.vm.sade.utils.slf4j.Logging

object TempElasticDockerClient {
  val url                   = s"http://localhost:${TempDockerElastic.start()}"
  val client: ElasticClient = ElasticClient(ElasticProperties(url))
}

private object TempDockerElastic extends Logging {

  import fi.vm.sade.utils.tcp.ChooseFreePort
  import scala.annotation.tailrec
  import scala.util.{Try, Failure, Success}

  private val port          = new ChooseFreePort().chosenPort
  private val containerName = "koutaexternal-elastic"

  def start(): Int = {
    try {
      if (!elasticIsRunning()) {
        startElasticContainer()
      }
      port
    } finally {
      Runtime.getRuntime.addShutdownHook(new Thread(() => stop()))
    }
  }

  private val elasticIsRunning: () => Boolean = () => {
    Try(runBlocking(s"curl --silent 127.0.0.1:$port/_cluster/health")) match {
      case Success(value) => value == 0
      case Failure(_)     => false
    }
  }

  private def startElasticContainer(): Unit = {
    logger.info("Starting Elasticsearch container:")
    runBlocking(
      //s"docker run --rm -d --name $containerName --env discovery.type=single-node -p 127.0.0.1:$port:9200 docker.elastic.co/elasticsearch/elasticsearch:7.10.2"
      s"docker run --rm -d --name $containerName --env discovery.type=single-node -p 127.0.0.1:$port:9200 docker.elastic.co/elasticsearch/elasticsearch:6.8.13"
    )

    if (!elasticHasStarted()) {
      throw new RuntimeException(s"Elasticsearch not accepting connections at port $port")
    }
  }

  private def elasticHasStarted(): Boolean = {

    @tailrec
    def tryTimes(times: Int)(thunk: () => Boolean): Boolean = {
      times match {
        case n if n < 1 => false
        case 1          => thunk()
        case n =>
          thunk() || {
            Thread.sleep(1000)
            tryTimes(n - 1)(thunk)
          }
      }
    }

    tryTimes(30)(elasticIsRunning)
  }

  def stop(): Unit = {
    runBlocking(s"docker kill $containerName")
  }

  private def runBlocking(command: String): Int = {
    import scala.sys.process.stringToProcess

    val returnValue = command.!
    if (returnValue != 0) {
      throw new RuntimeException(s"Command '$command' exited with $returnValue")
    }
    returnValue
  }
}
