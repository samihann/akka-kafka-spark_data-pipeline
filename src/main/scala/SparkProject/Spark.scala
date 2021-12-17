package SparkProject

import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import java.util.logging.Level
import org.apache.spark.sql.{ForeachWriter, Row, SparkSession}
import org.apache.spark.sql.functions.{col, date_format, second, to_timestamp, trunc, window}
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.streaming.StreamingContext.rddToFileName
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext, Time}
import org.slf4j.{Logger, LoggerFactory}

import java.io.File
import java.text.SimpleDateFormat
import java.util.Properties
import java.util.regex.Pattern
import javax.activation.{DataHandler, FileDataSource}
import scala.concurrent.duration.DurationInt
import javax.mail._
import javax.mail.internet._
import scala.math.Ordered.orderingToOrdered
import scala.sys.process.Process
object Spark {


  def main(args:Array[String]): Unit = {
    val logger: Logger = LoggerFactory.getLogger(this.getClass.getSimpleName)

    // Setting up Kafka topic info
    val brokerId = "localhost:9092"
    val groupId = "GRP1"
    val topics = "topic1"
    val offset = "earliest"
    val sparkConf = new SparkConf().setMaster("local[2]").setAppName("SparkWordCount")

    // Setting spark window of 60 seconds
    val ssc = new StreamingContext(sparkConf, Seconds(60))

    val sc = ssc.sparkContext
    sc.setLogLevel("OFF")

    val topicSet = topics.split(",").toSet

    logger.info("Setting Kafka Parameters")
    val kafkaParams = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> brokerId,
      ConsumerConfig.GROUP_ID_CONFIG -> groupId,
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> offset,
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer]
    )

    logger.info("Binding Kafka Parameters and topic")
    // Consumer will source from Kafka where it is subscribing to topic - topic1
    val messages = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topicSet, kafkaParams)
    )

    logger.info("Extract Message type from the logs")
    val msg_types = Set("ERROR", "INFO", "WARN","DEBUG")
    val words = messages.map(_.value()).flatMap(_.split(" ")).filter(msg_types.contains(_))

    logger.info("Map Reduce the logs on the basis of Log Message Type")
    val countwords = words.map(x => (x, 1)).reduceByKey(_ + _)

    logger.info("Print the results on the screen")
    countwords.print()

    logger.info("Extracting the Report Save Location and Location of Email service script")
    val config = ConfigFactory.load()
    val reportSaveLocation = config.getString("emailservice.report-save-location")
    val emailServiceLocation = config.getString("emailservice.email-service")
    val emailService = "sh "+emailServiceLocation

    countwords.foreachRDD(rdd => {
      val map = rdd.collect().toMap
      val count: Int = map.getOrElse("ERROR",0)
      // If the number of ERROR messages in the time window is >1, save the report and trigger email script
      if(count > 1)
      {
        sc.parallelize(map.toSeq).saveAsTextFile(reportSaveLocation)
        Process(emailService)!
      }
      else
      {
        logger.info("COUNT IS LESS THAN 2")
      }
    })

    ssc.start()

    ssc.awaitTermination()

  }
}