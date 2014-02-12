import _root_.sbt.Keys._

organization := "org.apache.flume.flume-ng-channels"

name := "Flume.RabbitMQ"

version := "1.1-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.apache.flume" % "flume-ng-core" % "1.2.0-cdh4.1.2",
  "com.rabbitmq" % "amqp-client" % "2.8.2",
  "junit" % "junit" % "4.10" % "test"
)

resolvers += "CDH3" at "https://repository.cloudera.com/artifactory/cloudera-repos/"
