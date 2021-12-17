name := "kafka-spark"

version := "0.1"

scalaVersion := "2.11.12"


val sparkVersion = "2.3.4"

resolvers += Resolver.jcenterRepo
assemblyMergeStrategy in assembly := {
  case "reference.conf" => MergeStrategy.concat
  case "META-INF/services/org.apache.spark.sql.sources.DataSourceRegister" => MergeStrategy.concat
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}

libraryDependencies ++= Seq(

  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.kafka" % "kafka-clients" % "2.3.0",
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion,
  "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion,
  "com.amazonaws" % "aws-java-sdk" %  "1.11.152",
  "javax.mail" % "mail"  % "1.4",
  "com.typesafe" % "config" % "1.2.1"
)

libraryDependencies += "com.typesafe.akka" %% "akka-stream-kafka" % "1.0-M1"

val logbackVersion = "1.3.0-alpha10"
val scalacticVersion = "3.2.9"

libraryDependencies += "ch.qos.logback" % "logback-core" % logbackVersion
libraryDependencies += "ch.qos.logback" % "logback-classic" % logbackVersion
libraryDependencies += "org.scalactic" %% "scalactic" % scalacticVersion
libraryDependencies += "org.scalatest" %% "scalatest" % scalacticVersion % Test
libraryDependencies += "org.scalatest" %% "scalatest-featurespec" % scalacticVersion % Test