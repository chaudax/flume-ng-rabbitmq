import AssemblyKeys._

seq(assemblySettings: _*)

organization := "org.apache.flume.flume-ng-channels"

name := "Flume.RabbitMQ"

version := "1.1-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.apache.thrift" % "libthrift" % "0.9.1" % "provided",
  "org.apache.flume" % "flume-ng-core" % "1.4.0-cdh4.5.0" % "provided",
  "org.apache.flume" % "flume-ng-sdk" % "1.4.0-cdh4.5.0" % "provided",
  "com.rabbitmq" % "amqp-client" % "3.2.3",
  "junit" % "junit" % "4.10" % "test"
)

resolvers += "CDH4" at "https://repository.cloudera.com/artifactory/cloudera-repos/"

resolvers += "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "com.github.sstone" %% "amqp-client" % "1.3-ML3",
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "com.typesafe.akka" %% "akka-slf4j" % "2.2.3")