
import org.scalatest.funspec.AnyFunSpec
import com.typesafe.config.ConfigFactory
import scala.sys.process._
import scala.io.Source



class StreamingDataPipelineTestSuite extends AnyFunSpec {
  val config = ConfigFactory.load()

  describe("Config file") {
    it("should be present") {
      assert(!config.isEmpty)
    }
  }

  describe("Config parameters"){
    it("should have correct dispatcher") {
      assert(config.getString("akka.kafka.producer.use-dispatcher") == "akka.kafka.default-dispatcher")
    }
    it("should have a topic name") {
      assert(config.getString("spark.topic-name") == "topic1")
    }
  }

  describe("Map Reduce"){
    it("should count number of ERROR messages") {
      val conf = ConfigFactory.load()
      val filename = conf.getString("sampleLogFileLocation")
      val buf = scala.collection.mutable.ListBuffer.empty[Int]
      val lines = Source.fromFile(filename).getLines.toList
      lines.foreach(line => {
        val logEntry = line.split(" - ").map(_.trim)
        val logParser = logEntry(0).replace("  ", " ").split(" ").map(_.trim)
        val logMsgType = logParser(2)
        if (logMsgType=="ERROR") {
          buf += 1
        }
      })

      assert(buf.size > 1)
    }
  }

  describe("Shell command"){
    it("should execute script") {
      val result = Process("sh Shell-Scripts/testScript.sh").!!

      result === "Hello"
    }
  }
}
