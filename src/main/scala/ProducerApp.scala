import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object ProducerApp extends App {
  val logger: Logger = LoggerFactory.getLogger(this.getClass.getSimpleName)

  // Actors are the unit of execution in Akka.
  // The Actor model is an abstraction that makes it easier to write correct concurrent, parallel and distributed systems.

  implicit val system: ActorSystem = ActorSystem("producer-sys") // AkkaSystem is a home to hierarchy of actors.
  implicit val mat: Materializer = ActorMaterializer() // ActorMaterializer can materialize stream blueprints as running streams.
  implicit val ec: ExecutionContextExecutor = system.dispatcher //thread pool - to achieve concurrency
  logger.info("Created ActorSystem and ActorMaterializer instances.")

  // Load configuration
  val config = ConfigFactory.load()
  logger.info("Loaded configuration.")

  // Get configuration for producer
  val producerConfig = config.getConfig("akka.kafka.producer")

  // Create producer settings
  val producerSettings = ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)

  // Get log file
  val sourceLogFile = config.getString("emailservice.source-log-file")
  logger.info("Log file loaded.")

  // Parse and split log file
  val lines = scala.io.Source.fromFile(sourceLogFile).mkString
  val result = lines.split("\n").toList // Log events in a list
  logger.info("Log file parsed.")

  // Get topic name
  val topicName = config.getString("spark.topic-name")

  logger.info("Sending log events via Akka streams to Kafka producer.")
  // Produces Akka streams with stream of log events as data
  val produce: Future[Done] = {
      Source(result)
        .map(value => new ProducerRecord[String, String](topicName, value.toString))
        .runWith(Producer.plainSink(producerSettings))
      // Source() - Starts a new Akka Stream Source from the given Iterable.
      // ProducerRecord - Sends a key, value pair. Here key is topic name and value is individual record.
      // Every Subscriber directly attached to the Publisher of this stream will see an individual flow of elements
      // runWith - Connects the Akka stream source of a list of log events to the Kafka Producer sink. Kafka producer is sink to the akka stream and is source to Spark.


  }

  //    Source(1 to 100)
//      .map(value => new ProducerRecord[String, String]("topic1", value.toString))
//      .runWith(Producer.plainSink(producerSettings))

  // On complete log if completed or not
  produce onComplete  {
    case Success(_) => logger.info("Done"); system.terminate()
    case Failure(err) => logger.info(err.toString); system.terminate()
  }
}